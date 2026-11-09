import socket
import threading
import os

HOST = "127.0.0.1"
PORT = 12354

def formatar_mensagem(mensagem: str) -> str:
    partes = mensagem.split("|", 2)
    if len(partes) == 3 and partes[0] == "MSG":
        return f"{partes[1]}: {partes[2]}"
    if len(partes) >= 2 and partes[0] == "SERVIDOR":
        return f"[Servidor] {partes[1]}"
    return mensagem


saindo = threading.Event()


def receber(sock: socket.socket):
    buffer = ""
    while True:
        try:
            dados = sock.recv(4096)
        except OSError:
            break
        if not dados:
            break

        buffer += dados.decode("utf-8", errors="replace")
        while "\n" in buffer:
            linha, buffer = buffer.split("\n", 1)
            linha = linha.strip()
            if linha:
                print("\r" + formatar_mensagem(linha), flush=True)
                print("> ", end="", flush=True)

    if saindo.is_set():
        return
    print("Conexao encerrada pelo servidor.", flush=True)
    os._exit(0)  


def main():
    print(f"Conectando em {HOST}:{PORT} ...", flush=True)
    try:
        sock = socket.create_connection((HOST, PORT), timeout=5)
        sock.settimeout(None)
    except OSError as e:
        print(f"Nao foi possivel conectar ao servidor {HOST}:{PORT}: {e}")
        return

    os.system("") 

    with sock:
        print("")
        nome = input("Digite seu nome: ").strip()
        while not nome:
            nome = input("Nome invalido. Digite seu nome: ").strip()

        threading.Thread(target=receber, args=(sock,), daemon=True).start()

        print("Conectado ao servidor. Digite sua mensagem e 'SAIR' para encerrar.", flush=True)
        print("")
        sock.sendall(f"JOIN:{nome}\n".encode("utf-8"))

        try:
            while True:
                texto = input("> ").strip()
                print("\033[A\r\033[K", end="", flush=True)
                if texto.upper() == "SAIR":
                    saindo.set()
                    sock.sendall(b"QUIT\n")
                    break
                if texto:
                    sock.sendall(f"MSG:{texto}\n".encode("utf-8"))
                    print(f"eu: {texto}", flush=True)
        except (KeyboardInterrupt, EOFError):
            saindo.set()


if __name__ == "__main__":
    main()