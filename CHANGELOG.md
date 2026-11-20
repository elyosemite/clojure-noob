# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](https://keepachangelog.com/).

## [Unreleased]
### Added
- US-02: ordem de venda limitada (`create-sell-order`, `place-sell-order!`).
- Lado de venda no order book (asks em prioridade preço-tempo), `best-bid`, `best-ask` e `spread`.
- Livro completo lado a lado (compra | venda) com spread no terminal (`format-book`).
- US-01: ordem de compra limitada (`create-buy-order`) com validação de ativo, quantidade e preço (`bigdec`).
- Order book puro (`domain/order-book`) com lado de compra em prioridade preço-tempo.
- Repositório em memória (`repository/db`) e serviço `place-buy-order!` (`service/trading-service`).
- Saída no terminal (`presentation/terminal`) e demonstração de compras no `-main`.

### Changed
- `format-buy-result` e `format-bids` substituídos por `format-order-result` e `format-book`.
- `-main` não executa mais os exemplos de `learning/*`.
- Arquitetura passa a incluir order book e matching engine (seção 5.8).

### Fixed
- Acentos corrompidos no terminal do Windows (JVM com stdout/stderr em UTF-8).
- Erro de compilação em `domain/order` (`System/currentTimeMillis`).
- Erro de compilação em `learning/helloworld` (`str`).
