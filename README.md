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

Quando uma ordem nova cruza o lado oposto (compra >= melhor venda, ou venda <=
melhor compra), ela é executada ao preço da ordem que já estava no livro,
podendo consumir vários níveis de preço. O saldo não executado fica no livro:

    [COMPRA]   #9 PETR4   100 @ 31.30  OK executada
        TRADE PETR4    80 @ 31.20 (compra #9 x venda #5)
        TRADE PETR4    20 @ 31.20 (compra #9 x venda #7)
    [VENDA]   #10 PETR4   120 @ 30.80  OK executada parcialmente (resta 70)
        TRADE PETR4    50 @ 31.00 (compra #2 x venda #10)

Ordens abertas ou parcialmente executadas podem ser canceladas; o saldo sai do
livro. Ordens já executadas, já canceladas ou inexistentes são rejeitadas:

    [CANCELA]   #4 PETR4   200 @ 30.50  OK cancelada
    [CANCELA]   #7 PETR4    10 @ 31.20  OK cancelada
    [CANCELA]   #5  REJEITADO: ordem já executada
    [CANCELA]  #99  REJEITADO: ordem não encontrada: #99

Cada ordem pertence a um investidor cadastrado. Compra reserva
`quantidade x preço limite` do saldo; venda reserva os ativos. Sem saldo ou
ativos suficientes a ordem é rejeitada. Cada trade liquida as duas carteiras
(o comprador recebe de volta a diferença entre o limite e o preço do negócio)
e o cancelamento devolve a reserva. Ao final, a demonstração imprime o extrato:

    === CARTEIRA #1 Ana ===
      saldo: 9650.00 (reservado 4360.00)
      PETR4:    150
      VALE3:     20

    === CARTEIRA #2 Bruno ===
      saldo: 5990.00
      PETR4:    180 (reservado 170)
      VALE3:     80

Rodar os testes:

    $ lein test

## Estrutura

    src/clojure_noob/
      domain/        regras puras (order, order-book, matching, trade, market, wallet, user)
      repository/    estado em memória (atom)
      service/       casos de uso (trading-service, user-service)
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
