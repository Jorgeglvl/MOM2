package client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import common.RemoteClient;
import common.RemoteServer;

public class Client extends UnicastRemoteObject implements RemoteClient {
	
	private static final long serialVersionUID = 1L;
	protected String nome;
	private Registry registro;
	private RemoteServer server;
	private Home janela;
	private String host, nickname;
	private int porta;
	
	public Client(Home janela, String ip, int port, String nickname) throws RemoteException{
		this.janela = janela;
		
		host = ip;
		porta = port;
		this.nickname = nickname;
		
		try {
			registro = LocateRegistry.getRegistry(porta);
			server = (RemoteServer)registro.lookup("//"+host+":"+porta+"/Servidor");
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Não foi possivel conectar com o servidor");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public boolean connect(){
		
		try {
			nome = this.nickname;
			if(nome!=null) {
				int resposta = server.conectaUsuario(this);
				janela.setMensagemLog("Codigo do retorno: "+resposta);
				if(resposta==-1) {
					JOptionPane.showMessageDialog(null, "Usuario '"+nome+"' ja está conectado existe");
				}
				else {
					janela.setMensagemLog("Connectado");
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean enviaMensagem(String nome, String conteudoMsg, boolean tipoFila) {
		
		try {
			if(tipoFila) {
				if(!server.produzMensagemFila(nome, conteudoMsg)) {
					JOptionPane.showMessageDialog(null, "Usuario nao existe");
					return false;
				}
			}
			else {
				if(!server.produzMensagemTopico(nome, conteudoMsg)) {
					JOptionPane.showMessageDialog(null, "Topico nao existe");
					return false;
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public ArrayList<String> recebeMensagem(String nome, boolean tipoFila) {
		
		try {
			if(tipoFila) {
				return server.recebeMensagemFila(nome);
			}
			else {
				if(!server.assinaTopico(nome, this.nome)) {
					JOptionPane.showMessageDialog(null, "Você já está inscrito");
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}
	
	public void notificaMensagem() throws RemoteException {
		janela.recebeMensagensUsuarios();
	}
	
	public void notificaDesconexao() throws RemoteException {
		JOptionPane.showMessageDialog(null, "Você foi desconectado");
		System.exit(0);
	}
	
	public void setMensagemTopico(String mensagem) throws RemoteException {
		janela.escreveMensagensTopico(mensagem);
	}
	
	public ArrayList<String> getUsuarios() {
		try {
			return server.getUsuariosOnline();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}
	
	public ArrayList<String> getTopicos() {
		try {
			return server.getTopicosDisponiveis();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}
	
	public String getNome() throws RemoteException {
		return nome;
	}
}
