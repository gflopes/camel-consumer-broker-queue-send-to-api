# camel-consumer-broker-queue-send-to-api
Projeto Apache Camel - Consumidor de Mensagens no ActiveMQ enviando via HTTP para uma API Fake

Pré Requisitos:

- JDK 1.8
- Instância do ActiveMQ
  - Download da Imagem: docker pull rmohr/activemq
  - Execução do Container: docker run -p 61616:61616 -p 8161:8161 -p 5672:5672 rmohr/activemq
- Mockoon para a API Fake (POST) https://mockoon.com/

