# TP_2_AEDs_III
Repositório do Trabalho Prático II de Algoritmos e Estrutura de Dados III

#### Membros do grupo:
- Luís Augusto Lima de Oliveira
- Victor Ferraz de Moraes

# Relatório
## Implementações feitas pelo grupo
Foram modificadas as funções CRUD da classe ArquivoLivros.java para incluir a Lista invertida em todas as suas operações. 

####  Create
- Foi realizado o tratamento sobre o título do livro a ser criado, em que utilizamos a biblioteca java.text.Normalizer para dissolver as acentuações na String, para em seguida remover os acentos com um .replaceAll. Em seguida, é utilizado o método .toLowerCase() para converter todos os caracteres da String em sua versão minúscula. (Esse mesmo tratamento foi utilizado no decorrer do código para qualquer manipulação de Strings envolvendo a Lista invertida)
- Após tratada a String, todas as palavras são separadas através do método .split(" ")
- Para cada palavra no array de Strings é feita uma verificação se a palavra é uma stop word.
- Caso não seja uma stop word, ela é adicionada ao dicionário da lista invertida com o respectivo id do livro inserido.

#### Read
- A String de pesquisa é tratada e cada palavra é separada.
- Utilizando a função read da classe ListaInvertida, é feito a pesquisa em cima da primeira palavra válida (enquanto não é uma stopword avança para a próxima palavra).
- Um array de IDs é retornado e armazenado para esta palavra.
- A partir de um array temporário de IDs, é feito a pesquisa para as próximas palavras válidas.
- Para cada palavra, é realizado a interseção entre o array de IDs temporário e o array de IDs principal.
- É feito esse procedimento até que todas as palavras do array de Strings sejam verificadas ou que o array de IDs esteja vazio.
- Após a verificação ser concluída, é criado um array de Livros com tamanho sendo a quantidade de IDs encontrados, sendo esses livros posteriormente buscados e armazenados a partir do método read da super classe.
- Esse array de livros é posteriormente retornado e utilizado no mostrarLivros da classe MenuLivros

#### Update
- A String do titulo antigo e do titulo novo são tratadas e cada palavra é separada.
- Cada palavra do título antigo é deletada do dicionário com seu respectivo id da lista invertida.
- Cada palavra do título novo é inserida no dicionário com seu respectivo id da lista invertida.

#### Delete
- A String do titulo é tratada e cada palavra é separada.
- Cada palavra do título é deletada do dicionário com seu respectivo id da lista invertida.

## Resultados



### Checklist

#### - A inclusão de um livro acrescenta os termos do seu título à lista invertida?

<pre> Sim, todos os termos tratados com suas respectivas chaves. </pre>

#### - A alteração de um livro modifica a lista invertida removendo ou acrescentando termos do título?

<pre> Sim, os termos antigos são removidos e os novos acrescentados. </pre>

#### - A remoção de um livro gera a remoção dos termos do seu título na lista invertida?

<pre> Sim, cada termo com seu respectivo ID é removido da lista invertida. </pre>

#### - Há uma busca por palavras que retorna os livros que possuam essas palavras?

<pre> Sim, a busca retorna um array de livros que é utilizado no método mostraLivros.  </pre>

#### - Essa busca pode ser feita com mais de uma palavra?

<pre> Sim, assim como foi explicado no relatório inicial. </pre>

#### - As stop words foram removidas de todo o processo?

<pre> Sim, a partir de um arrayList que é criado na inicialização da classe ArquivoLivros.  </pre>

#### - Que modificação, se alguma, você fez para além dos requisitos mínimos desta tarefa?

<pre> Além do tratamento pedido, removemos qualquer caractere extra que esteja junto à palavra. Por exemplo, "Memória:" é tratado para "memoria". </pre>

#### - O trabalho está funcionando corretamente?

<pre> Sim, o trabalho funciona corretamente. </pre>

#### - O trabalho está completo?

<pre> Sim, o trabalho está completo. </pre>

#### - O trabalho é original e não a cópia de um trabalho de um colega?

<pre> Sim, o trabalho foi implemetado usando código desenvolvido pelo grupo utilizando apenas a base disponibilizada pelo Professor no Canvas. </pre>
