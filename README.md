# Breast-Cancer-Api

API principal da aplicação **Breast Cancer**, responsável por orquestrar o fluxo de negócio, integrar com o **Breast-Cancer-ML-Service**, persistir dados no banco e servir como camada oficial consumida pelo frontend.

## Objetivo

O `Breast-Cancer-Api` é o backend principal do sistema.
Ele atua como a porta de entrada da aplicação e tem como responsabilidade:

- receber requisições do frontend;
- validar dados de entrada;
- integrar com o microserviço de machine learning;
- persistir resultados no banco de dados;
- expor endpoints da aplicação de forma organizada e escalável.

Nesta V1, a API será responsável principalmente pela integração com o serviço de ML e pelo gerenciamento das análises realizadas.

---

## Papel na arquitetura

A arquitetura do projeto está dividida em três partes principais:

- **Breast-Cancer** → frontend da aplicação
- **Breast-Cancer-Api** → backend principal com regras de negócio e persistência
- **Breast-Cancer-ML-Service** → microserviço Python responsável pela inferência do modelo

### Fluxo da aplicação

```text
Frontend → Breast-Cancer-Api → Breast-Cancer-ML-Service