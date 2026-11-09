import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Cliente {
    private static volatile boolean saindo = false;
    private static final String HOST = "127.0.0.1";
    private static final int PORTA = 12354;

    public static void main(String[] args) {
        try (Scanner teclado = new Scanner(System.in)) {
            System.out.println("Conectando em " + HOST + ":" + PORTA + " ...");
            Socket clienteSocket = new Socket(HOST, PORTA);
            BufferedReader entradaServidor = new BufferedReader(
                    new InputStreamReader(clienteSocket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter saida = new PrintWriter(
                    new OutputStreamWriter(clienteSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            
            System.out.println("");
            System.out.print("Digite seu nome: ");
            String nome = teclado.nextLine().trim();
            while (nome.isEmpty()) {
                System.out.print("Nome invalido. Digite seu nome: ");
                nome = teclado.nextLine().trim();
            }

            Thread receptor = new Thread(() -> {
                try {
                    String mensagem;
                    while ((mensagem = entradaServidor.readLine()) != null) {
                        if (!mensagem.trim().isEmpty()) {
                            System.out.print("\r");
                            System.out.println(formatarMensagem(mensagem.trim()));
                            System.out.print("> ");
                        }
                    }
                    if (!saindo) {
                        System.out.println("Conexão encerrada pelo servidor.");
                    }
                } catch (IOException e) {
                }
                System.exit(0); 
            }, "receptor");
            receptor.setDaemon(true);
            receptor.start();

            System.out.println("Conectado ao servidor. Digite sua mensagem e 'SAIR' para encerrar.");
            System.out.println("");
            saida.println("JOIN:" + nome);

            while (true) {
                System.out.print("> ");
                if (!teclado.hasNextLine()) {
                    saindo = true;
                    break;
                }
                String texto = teclado.nextLine();
                if (texto.equalsIgnoreCase("SAIR")) {
                    saindo = true;
                    saida.println("QUIT");
                    break;
                }

                System.out.print("\033[A\r\033[K");
                if (!texto.trim().isEmpty()) {
                    saida.println("MSG:" + texto);
                    System.out.println("eu: " + texto);
                }
            }
            clienteSocket.close();
        } catch (IOException e) {
            System.out.println("Nao foi possivel conectar ao servidor: " + e.getMessage());
        }
    }

    private static String formatarMensagem(String mensagem) {
        String[] partes = mensagem.split("\\|", 3);

        if (partes.length == 3 && "MSG".equals(partes[0])) {
            return partes[1] + ": " + partes[2];
        }
        if (partes.length >= 2 && "SERVIDOR".equals(partes[0])) {
            return "[Servidor] " + partes[1];
        }
        return mensagem;
    }
}