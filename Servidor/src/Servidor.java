import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Servidor {
    private static final int PORTA = 12354;
    static final List<Conexao> clientes = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            System.out.println("Servidor de chat iniciado na porta " + PORTA);
            System.out.println("Aguardando clientes...");
            System.out.println("");

            while (true) {
                Socket socket = servidor.accept();
                try {
                    Conexao conexao = new Conexao(socket);
                    clientes.add(conexao);
                    System.out.println("");
                    System.out.println("Cliente conectado: "
                            + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                    System.out.println("Clientes conectados: " + clientes.size());
                    System.out.println("");
                    conexao.iniciar();
                } catch (IOException e) {
                    System.err.println("Falha ao preparar cliente: " + e.getMessage());
                    try { socket.close(); } catch (IOException ignorado) { }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor (porta " + PORTA
                    + " ja em uso por outro servidor?): " + e.getMessage());
        }
    }

    static void enviarParaTodos(String mensagem) {
        for (Conexao cliente : clientes) {
            if (cliente.estaRegistrado()) {
                cliente.enviar(mensagem);
            }
        }
    }

    static void enviarParaTodosExceto(Conexao origem, String mensagem) {
        for (Conexao cliente : clientes) {
            if (cliente != origem && cliente.estaRegistrado()) {
                cliente.enviar(mensagem);
            }
        }
    }
}