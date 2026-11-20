# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](https://keepachangelog.com/).

## [Unreleased]
### Added
- US-01: ordem de compra limitada (`create-buy-order`) com validação de ativo, quantidade e preço (`bigdec`).
- Order book puro (`domain/order-book`) com lado de compra em prioridade preço-tempo.
- Repositório em memória (`repository/db`) e serviço `place-buy-order!` (`service/trading-service`).
- Saída no terminal (`presentation/terminal`) e demonstração de compras no `-main`.

### Changed
- `-main` não executa mais os exemplos de `learning/*`.
- Arquitetura passa a incluir order book e matching engine (seção 5.8).

### Fixed
- Erro de compilação em `domain/order` (`System/currentTimeMillis`).
- Erro de compilação em `learning/helloworld` (`str`).
