# K1-DIMP Orquestrador

### Visão geral
O Sistema DIMP faz a leitura de dados do datalake do AZURE e traz essas informações para uma base de dados
PostGreSql acomodando essas informações em várias tabelas.

Pelo fato de essas tabelas serem muito extensas (algumas com mais de 700.000 registros), o tempo de carga dessas
tabelas é longo, e por esse motivo decidimos rodar vários serviços em paralelo, cada um fazendo a carga em uma
tabela.

Para tanto, fez-se necessário criar um módulo de nome DIMP - Orquestrador, que tem por finalidade distribuir o
trabalho em diversos PODS, um para cada tabela.

Cada POD bate no serviço do orquestrador solicitando uma tabela, e o orquestrador envia uma tabela da seqüência para
cada request.

Como os dados são volumosos, o orquestrador faz a descompactação dos dados do datalake e envia em "lotes" para as
instâncias da DIMP.
O tamanho desses lotes é definido pela variável de ambiente **DIMP_LOTE_ORQUESTRADOR**, valor recomendado: 1000

O DIMP - Orquestrador trabalha na porta **3971**

Existem arquivos que não estão mapeados pois não constam na documentação do projeto, mas que no entanto existem no
datalake, por este motivo, o DIMP - Orquestrador tem uma tabela de arquivos que são os que devem ser considerados, os
demais que existirem no datalake serão ignorados.

Esses arquivos, os que devem ser considerados encontram-se no arquivo **OrquestradorService.java** em uma lista chamada
**controle**

>		controle = new ArrayList<>();
>		controle.add("TbpagamentoTipo");
>		controle.add("TbMeioCaptura");
>		controle.add("TbIntermediadorServico");
>		controle.add("TbPessoa");
>		controle.add("TbEndereco");
>		controle.add("TbSeller");
>		controle.add("TbNotaFiscal");
>		controle.add("TbCliente");
>		controle.add("TbCancelamentoPagamento");
>		controle.add("TbVenda");


### Chamadas para o Orquestrador

@GetMapping("/getTabela")
Este comando envia a próxima tabela na lista para o POD requisitante.
Dispôe as linhas do datalake, em um lote de tamanho definido pela variável de ambiente **DIMP_LOTE_ORQUESTRADOR**.
As chamadas para os demais lotes são feitas pelo próximo endpoint.

@GetMapping("/getLote")
Esta chamada necessita de parâmetros. São eles:
- **tabela**: parâmetro que vem da chamada anterior.
- **indice**: iniciando com 1, incrementado a cada chamada, traz o lote informado por índice
- **tamanhoTabela**: parâmetro que vem com a primeira chamada, passado para evitar que o sistema tenha que calcular novamente o tamanho da tabela, diminuindo assim o tempo de processamento

@GetMapping("listaTabelas")
Traz uma lista das tabelas disponíveis

@GetMapping("/getSituacaoServico")
O orquestrador leva um tempo para descompactar as informações do datalake para deixá-las disníveis para os serviços, portanto essa chamada devolve "iniciando" ou "pronto", e deve estar nessa condição antes que os PODS da DIMP subam.

@GetMapping("/inicializaPonteiro")
Para retornar ao início a lista das tabelas a serem distribuidas sem necessitar parar e subir novamente o orquestrador.

