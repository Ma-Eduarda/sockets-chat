# Projeto Sockets — Chat

Aplicação de chat em grupo cliente-servidor, desenvolvida com sockets TCP. O servidor foi implementado em Java e aceita conexões de clientes Java e Python. As mensagens enviadas por um participante são encaminhadas aos demais participantes conectados.

## Funcionalidades

- Envio e recebimento de mensagens em tempo real;
- Identificação dos participantes por nome;
- Avisos quando alguém entra ou sai do chat;
- Cliente desenvolvido em Java;
- Cliente desenvolvido em Python;
- Comunicação TCP pela porta `12354`.

## Requisitos

- Java JDK instalado, para executar o servidor e o cliente Java;
- Python 3 instalado, para executar o cliente Python.

## Como executar

Abra três terminais, um para o servidor e os demais para os clientes. O servidor precisa estar em execução antes de conectar os clientes.

### 1. Iniciar o servidor

Na raiz do projeto:

```powershell
cd Servidor/src
javac Servidor.java Conexao.java
java Servidor
```

O servidor inicia na porta `12354` e permanece aguardando conexões.

### 2. Iniciar o cliente Java

Em outro terminal, na raiz do projeto:

```powershell
cd Cliente-Java/src
javac Cliente.java
java Cliente
```

### 3. Iniciar o cliente Python

Em outro terminal, na raiz do projeto:

```powershell
python Cliente-Python/Cliente.py
```

Ao conectar, cada cliente informa um nome e pode começar a enviar mensagens. Para encerrar a conexão, digite `SAIR`.

## Executar em outra máquina

Por padrão, os clientes conectam ao endereço `127.0.0.1`, que representa a própria máquina. Para conectar clientes de outros computadores, altere `HOST` nos arquivos `Cliente-Java/src/Cliente.java` e `Cliente-Python/Cliente.py` para o IP da máquina que executa o servidor. Por exemplo:

```java
private static final String HOST = "192.168.1.100";
```

```python
HOST = "192.168.1.100"
```

O endereço é apenas um exemplo; use o IP real do servidor. Os computadores precisam ter conectividade entre si, e a porta `12354` deve estar liberada no firewall do servidor.

## Estrutura do projeto

```text
Projeto-Sockets/
├── Servidor/
│   └── src/
│       ├── Servidor.java
│       └── Conexao.java
├── Cliente-Java/
│   └── src/
│       └── Cliente.java
└── Cliente-Python/
    └── Cliente.py
```
