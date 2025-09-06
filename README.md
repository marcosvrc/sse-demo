# SSE Demo - Demonstração de Server-Sent Events

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Índice

- [Sobre](#-sobre)
- [Conceito de SSE](#-conceito-de-sse)
- [Arquitetura](#-arquitetura)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Instalação e Execução](#-instalação-e-execução)
- [API Endpoints](#-api-endpoints)
- [Interface do Usuário](#-interface-do-usuário)
- [Considerações de Desempenho](#-considerações-de-desempenho)
- [Contribuições](#-contribuições)

## 📖 Sobre

Este projeto demonstra a implementação de Server-Sent Events (SSE) utilizando Spring Boot. A aplicação permite a transmissão de eventos em tempo real do servidor para clientes conectados através de um fluxo unidirecional persistente, sem a necessidade de polling contínuo por parte do cliente.

## 🧩 Conceito de SSE

### O que é Server-Sent Events (SSE)?

Server-Sent Events (SSE) é uma tecnologia web que permite a um servidor enviar atualizações para clientes via HTTP. Diferentemente do WebSocket, que fornece comunicação bidirecional, o SSE é unidirecional - apenas do servidor para o cliente. Esta característica o torna ideal para cenários onde os clientes precisam receber atualizações em tempo real, mas não necessitam enviar dados com frequência para o servidor.

### Características Principais do SSE

1. **Conexão HTTP Padrão**: Utiliza o protocolo HTTP tradicional, facilitando a implementação e integração com infraestrutura existente.
2. **Reconexão Automática**: Browsers implementam reconexão automática caso a conexão seja perdida.
3. **Formato de Evento Padronizado**: Os eventos são enviados em formato texto com campos específicos como `id`, `event`, `data`, etc.
4. **MIME Type Específico**: Utiliza o tipo de mídia `text/event-stream`.
5. **Compatível com Proxy e Firewall**: Como usa HTTP padrão, não apresenta problemas com firewalls corporativos (diferente do WebSocket).

### Comparação com Outras Tecnologias

| Característica | SSE | WebSocket | Long Polling |
|----------------|-----|-----------|--------------|
| Comunicação    | Unidirecional (servidor → cliente) | Bidirecional | Unidirecional com reconexões frequentes |
| Protocolo      | HTTP padrão | Protocolo WebSocket | HTTP padrão |
| Reconexão      | Automática | Manual | Manual |
| Overhead       | Baixo | Médio | Alto |
| Complexidade   | Baixa | Média | Baixa |

### Casos de Uso Ideais

- Feeds de notícias em tempo real
- Atualizações de status (ex: progresso de tarefas)
- Notificações e alertas
- Dashboards que atualizam em tempo real
- Monitoramento de métricas e logs

## 🏗️ Arquitetura

Este projeto segue uma arquitetura em camadas típica de aplicações Spring Boot:

```ascii
  ┌───────────────┐
  │    Cliente    │
  │  (Navegador)  │
  └───────┬───────┘
          │ HTTP
          ▼
┌─────────────────────┐
│    Spring Boot      │
│                     │
│  ┌───────────────┐  │
│  │  Controllers  │  │
│  └───────┬───────┘  │
│          │          │
│  ┌───────▼───────┐  │
│  │   Services    │  │
│  └───────┬───────┘  │
│          │          │
│  ┌───────▼───────┐  │
│  │    Models     │  │
│  └───────────────┘  │
└─────────────────────┘
```

### Fluxo de Funcionamento

1. **Conexão do Cliente**: O cliente conecta-se ao endpoint `/api/sse/subscribe`
2. **Instância do SseEmitter**: O servidor cria uma instância de `SseEmitter` para o cliente
3. **Registro do Emitter**: O emitter é armazenado em uma coleção thread-safe no `SseEmitterService`
4. **Envio de Eventos**: Eventos podem ser enviados:
   - Programaticamente via endpoint `/api/sse/publish`
   - Automaticamente via agendamento no `SchedulerService`
5. **Propagação de Eventos**: Quando um evento ocorre, todos os emissores registrados recebem o evento
6. **Desconexão**: Quando o cliente desconecta, o emissor é removido da coleção

## 📂 Estrutura do Projeto

```text
src/
├── main/
│   ├── java/
│   │   └── br/
│   │       └── com/
│   │           └── sse/
│   │               ├── controller/
│   │               │   ├── HomeController.java
│   │               │   └── SseController.java
│   │               ├── model/
│   │               │   └── SseEvent.java
│   │               ├── service/
│   │               │   ├── SchedulerService.java
│   │               │   └── SseEmitterService.java
│   │               └── SseDemoApplication.java
│   └── resources/
│       ├── static/
│       │   └── index.html
│       └── application.properties
└── test/
    └── java/
        └── br/
            └── com/
                └── sse/
                    └── SseDemoApplicationTests.java
```

### Detalhamento dos Componentes

#### Controllers

1. **SseController.java**
   - Responsável pelos endpoints relacionados ao SSE
   - Expõe endpoints para:
     - Inscrição em eventos (`/subscribe`)
     - Publicação de eventos (`/publish`)
   - Atua como interface entre o cliente e o serviço de emissor SSE

2. **HomeController.java**
   - Controller simples que redireciona a raiz (`/`) para o arquivo index.html
   - Facilita o acesso à interface de demonstração

#### Models

1. **SseEvent.java**
   - Representa a estrutura de um evento SSE
   - Contém:
     - `id`: Identificador único para o evento
     - `message`: Mensagem/conteúdo do evento
     - `timestamp`: Data/hora de criação do evento
   - Utilizado para serialização/deserialização em formato JSON

#### Services

1. **SseEmitterService.java**
   - Componente central para gerenciamento de emissores SSE
   - Principais responsabilidades:
     - Criação e gerenciamento de instâncias `SseEmitter`
     - Registro de callbacks para tratamento de timeout, erros, etc.
     - Envio de eventos para todos os clientes conectados
     - Gerenciamento de ciclo de vida dos emitters
   - Utiliza `CopyOnWriteArrayList` para thread-safety

2. **SchedulerService.java**
   - Implementa funcionalidade de agendamento de eventos periódicos
   - Utiliza a anotação `@Scheduled` do Spring para enviar eventos automáticos a cada minuto
   - Simula situações do mundo real onde eventos são gerados periodicamente

#### Application

1. **SseDemoApplication.java**
   - Ponto de entrada da aplicação Spring Boot
   - Configurado com `@EnableScheduling` para suportar o agendamento de tarefas

#### Frontend

1. **index.html**
   - Interface web para demonstração e teste do SSE
   - Recursos:
     - Botões para conectar/desconectar do fluxo SSE
     - Campo para envio de mensagens personalizadas
     - Lista de eventos recebidos com timestamp
     - Indicador de status da conexão
   - Utiliza JavaScript nativo para conectar-se e manipular eventos SSE

## 🔧 Tecnologias Utilizadas

### Backend

- **Java 21**: Linguagem de programação principal
- **Spring Boot 3.5.5**: Framework para criação de aplicações Java
- **Spring Web**: Dependência para funcionalidades web e REST
- **Spring Scheduling**: Para agendamento de tarefas periódicas
- **SseEmitter**: API do Spring para implementar Server-Sent Events

### Cliente

- **HTML5**: Estrutura da página web
- **CSS3**: Estilização da interface
- **JavaScript**: Lógica de cliente para conexão SSE e manipulação de eventos
- **EventSource API**: API nativa do browser para conexões SSE

## 🚀 Instalação e Execução

### Pré-requisitos

- Java 21+
- Maven 3.8+
- Git

### Clonando o Repositório

```bash
git clone https://github.com/marcosvrc/sse-demo.git
cd sse-demo
```

### Compilando e Executando

```bash
./mvnw clean package
./mvnw spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8081`

## 📡 API Endpoints

### Endpoints SSE

#### Inscrição para Receber Eventos

- **URL**: `/api/sse/subscribe`
- **Método**: GET
- **Cabeçalhos de Resposta**:
  - `Content-Type: text/event-stream`
  - `Cache-Control: no-cache`
  - `Connection: keep-alive`
- **Descrição**: Estabelece uma conexão SSE persistente

#### Publicação de Eventos

- **URL**: `/api/sse/publish`
- **Método**: POST
- **Parâmetros**:
  - `message`: Mensagem a ser enviada a todos os clientes conectados
- **Resposta**: 200 OK com mensagem de confirmação
- **Descrição**: Envia uma mensagem para todos os clientes conectados

## 🖥️ Interface do Usuário

A aplicação inclui uma interface web simples para testar e demonstrar o funcionamento do SSE:

- **Conexão/Desconexão**: Botões para gerenciar a conexão SSE
- **Envio de Mensagens**: Campo para envio de mensagens personalizadas
- **Visualização de Eventos**: Lista com todos os eventos recebidos
- **Status de Conexão**: Indicador visual do estado da conexão

## 🚦 Considerações de Desempenho

### Gerenciamento de Recursos

- O uso de `CopyOnWriteArrayList` para armazenar emissores garante thread-safety, mas pode impactar o desempenho em cenários com muitas modificações
- Emissores são removidos automaticamente quando a conexão é fechada, evitando vazamentos de memória

### Escalabilidade

- Para ambientes de produção com muitos clientes, considerar:
  - Implementar clustering com Redis ou outra solução para distribuir eventos entre nós
  - Utilizar broker de mensagens (ex: Kafka, RabbitMQ) para cenários de alta carga
  - Implementar mecanismos de backpressure para clientes lentos

### Timeout e Reconexão

- Por padrão, os emissores são configurados com `Long.MAX_VALUE` para tempo de expiração
- O cliente implementa reconexão automática em caso de falha
- A aplicação trata adequadamente casos de desconexão e erros

## 🤝 Contribuições

Contribuições são bem-vindas! Para contribuir:

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/amazing-feature`)
3. Commit suas mudanças (`git commit -m 'Add some amazing feature'`)
4. Push para a branch (`git push origin feature/amazing-feature`)
5. Abra um Pull Request

---

Desenvolvido como demonstração técnica das capacidades de Server-Sent Events com Spring Boot.
