# camel-consumer-broker-queue-send-to-api
Projeto Apache Camel - Consumidor de Mensagens no ActiveMQ enviando via HTTP para uma API Fake

Pré Requisitos:

- JDK 1.8
- Docker
- Instância do ActiveMQ
  - Download da Imagem: docker pull rmohr/activemq
  - Execução do Container: docker run -p 61616:61616 -p 8161:8161 -p 5672:5672 rmohr/activemq
- Mockoon para a API Fake (POST) https://mockoon.com/
- FakeSMTP para envio de emails http://nilhcem.com/FakeSMTP/index.html
  - java -jar fakeSMTP-2.0.jar

