# clojure-noob

Order book (livro de ofertas) simulado para o mercado financeiro, escrito em Clojure
para estudo. Estado apenas em memória, sem banco de dados.
Arquitetura: [doc/architecture-guidelines.md](doc/architecture-guidelines.md).

## Requisitos

- Java 11+
- [Leiningen](https://leiningen.org/)

## Uso

Rodar a demonstração (envia ordens de compra e venda e imprime o livro):

    $ lein run

Exemplo de saída:

    [COMPRA]   #1 PETR4   100 @ 30.50  OK registrada
    [COMPRA]   #2 PETR4    50 @ 31.00  OK registrada
    [VENDA]    #5 PETR4    80 @ 31.20  OK registrada
    [VENDA]    #6 PETR4   100 @ 31.50  OK registrada
    [COMPRA]      VALE3     0 @ 65.00  REJEITADA: quantidade deve ser um inteiro > 0

    === LIVRO PETR4 ===
            COMPRA         |        VENDA
       #2     50 @   31.00 |    #5     80 @   31.20
       #1    100 @   30.50 |    #6    100 @   31.50
    spread: 0.20

Compra é ordenada do maior para o menor preço e venda do menor para o maior;
no empate de preço, a ordem mais antiga vem primeiro.

Rodar os testes:

    $ lein test

## Estrutura

    src/clojure_noob/
      domain/        regras puras (order, order-book, market, wallet, user)
      repository/    estado em memória (atom)
      service/       casos de uso (trading-service)
      presentation/  formatação para o terminal
      learning/      exercícios de estudo de Clojure

## License

Copyright © 2025 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
https://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
