import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Conexao {
    private final Socket socket;
    private final BufferedReader entrada;
    private final PrintWriter saida;
    private volatile String nomeUsuario;
    private volatile boolean ativo = true;

    public Conexao(Socket socket) throws IOException {
        this.socket = socket;
        this.entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.saida = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), false);
    }

    public boolean estaRegistrado() {
        return nomeUsuario != null && ativo;
    }

    public void iniciar() {
        Thread thread = new Thread(() -> {
            try {
                String mensagem;
                while (ativo && (mensagem = entrada.readLine()) != null) {
                    if (mensagem.trim().isEmpty()) {
                        continue;
                    }
                    processarMensagem(mensagem.trim());
                }
            } catch (IOException e) {
                if (ativo) {
                    System.out.println((nomeUsuario != null ? nomeUsuario : "Um cliente")
                            + " saiu sem avisar (conexao interrompida).");
                }
            } finally {
                fechar();
            }
        }, "conexao-" + socket.getPort());
        thread.start();
    }

    public synchronized void enviar(String mensagem) {
        saida.print(mensagem + "\n");
        saida.flush();
        if (saida.checkError()) {     
            ativo = false;
            Servidor.clientes.remove(this);
            try { socket.close(); } catch (IOException ignorado) { }
        }
    }

    private void processarMensagem(String mensagem) {
        if (mensagem.startsWith("JOIN:")) {
            String nome = mensagem.substring("JOIN:".length()).trim().replace("|", "_");
            if (nome.isEmpty()) {
                enviar("SERVIDOR|Nome invalido. Use JOIN:nome");
                return;
            }
            if (nomeUsuario != null) {
                enviar("SERVIDOR|Voce ja esta registrado como " + nomeUsuario);
                return;
            }

            this.nomeUsuario = nome;  
            System.out.println("");
            System.out.println("Usuario " + nome + " entrou no chat.");
            Servidor.enviarParaTodosExceto(this, "SERVIDOR|" + nome + " entrou no chat.");
            if (contarDestinatarios() == 0) {
                enviar("SERVIDOR|Nenhum outro usuario conectado no momento.");
            }
            return;
        }

        if (mensagem.equalsIgnoreCase("QUIT")) {
            ativo = false;   
            return;
        }

        if (nomeUsuario == null) {
            enviar("SERVIDOR|Registre seu nome antes de enviar mensagens: JOIN:nome");
            return;
        }

        String conteudo = mensagem;
        if (mensagem.startsWith("MSG:")) {
            conteudo = mensagem.substring("MSG:".length()).trim();
        }
        if (conteudo.isEmpty()) {
            enviar("SERVIDOR|Mensagem vazia.");
            return;
        }

        String pacote = "MSG|" + nomeUsuario + "|" + conteudo;
        System.out.println(nomeUsuario + " mandou mensagem para " + nomesDestinatarios() + ": " + conteudo);
        if (contarDestinatarios() == 0) {
            enviar("SERVIDOR|Ninguem recebeu: nao ha outros usuarios conectados.");
            return;
        }
        Servidor.enviarParaTodosExceto(this, pacote);
    }

    private int contarDestinatarios() {
        int n = 0;
        for (Conexao c : Servidor.clientes) {
            if (c != this && c.estaRegistrado()) {
                n++;
            }
        }
        return n;
    }

    private String nomesDestinatarios() {
        StringBuilder nomes = new StringBuilder();
        for (Conexao c : Servidor.clientes) {
            if (c != this && c.estaRegistrado()) {
                if (nomes.length() > 0) {
                    nomes.append(", ");
                }
                nomes.append(c.nomeUsuario);
            }
        }
        return nomes.length() == 0 ? "ninguem" : nomes.toString();
    }

    private void fechar() {
        ativo = false;
        Servidor.clientes.remove(this);

        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Erro ao fechar socket: " + e.getMessage());
        }

        System.out.println("Cliente desconectado" + (nomeUsuario != null ? " (" + nomeUsuario + ")" : "") + ".");
        System.out.println("Clientes conectados: " + Servidor.clientes.size());

        if (nomeUsuario != null) {
            Servidor.enviarParaTodos("SERVIDOR|" + nomeUsuario + " saiu do chat.");
            Conexao restante = null;
            int registrados = 0;
            for (Conexao c : Servidor.clientes) {
                if (c.estaRegistrado()) {
                    registrados++;
                    restante = c;
                }
            }
            if (registrados == 1) {
                restante.enviar("SERVIDOR|Nenhum outro usuario conectado no momento.");
            }
        }
    }
}