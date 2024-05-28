# TP_3_AEDs_III
Repositório do Trabalho Prático III de Algoritmos e Estrutura de Dados III

#### Membros do grupo:
- Luís Augusto Lima de Oliveira
- Victor Ferraz de Moraes

# Relatório
## Implementações feitas pelo grupo

####  Criação da Classe ArquivoLZW

* `void codifica(String dir, String backupComplement)`
  * Recebe o diretório base e a pasta destino dos Backups, que será aninhada ao diretório base. (Caso não exista diretório de backup, será criado um.)
  * A partir da Classe Date, é criado o arquivo de backup e backup-names contendo a data atual em seus nomes separados por um hífen. Dentro dos arquivos conterá os dados codificados dos arquivos .db e de seus nomes, respectivamente.
  * A partir da função List() da classe File, é retornado o nome de todos os arquivos e diretórios inclusos dentro do diretório base.
  * É criado um cabeçalho no início do arquivo de saída, que conterá o número de arquivos e diretórios, em seguida, é alocado um espaço para o endereçamento, que será utilizado para determinar a posição final de cada arquivo. 
  * Para cada nome lido, verifica se existem diretórios ou arquivos que não possuem o formato .db, ignorando todos eles e atualizando o ponteiro do arquivo atual. Caso o arquivo esteja no formato .db, ele será escrito na saída de nomes. Depois disso, é inicializado o dicionário e o arquivo a ser codificado será lido de forma sequencial byte a byte ao mesmo tempo que o resultado de sua codificação é escrito no caminho de saída.

* `void decodifica(String fileName, String backupDirPath, String restoredPath)`
  * Recebe o nome do arquivo a ser restaurado, assim como o caminho deste arquivo e o diretório onde será armazenado a descompressão. (Se o diretório de descompressão não existir, será criado um para tal).
  * Verifica se o arquivo de backup e backup-names existem para seguir a execução.
  * Lê do arquivo de backup todos os ponteiros de parada localizados no cabeçalho, assim como cada nome dentro de backup-names.
  * Para cada ponto de parada do cabeçalho em backup, caso o ponto de parada seja igual ao ponto de escrita, a iteração será pulada. Caso contrário, é criado um novo arquivo identificado pelo nome lido de backup-names.
  * Em seguida, com o dicionário inicilizado, é feita a descompressão do arquivo backup do ponto atual até o ponto de parada para cada arquivo restaurado, que serão armazenados dentro da pasta restored.

* `void atualizarPontosDeParada(RandomAccessFile out)`
  * Vai ao início do cabeçalho do arquivo backup, atualiza a quantidade de pontos de parada e armazena um novo ponto de parada no final do cabeçalho.

####  Criação da Classe MenuBackup

* `void menu()`
  * Um menu de opções, realizado com Switch Case, mostrando as opções: 1) Fazer Backup, 2) Restaurar Backup e 0) Retornar ao Menu anterior.
  * Fazer Backup chama a função `codifica(...)` da classe `ArquivoLZW`.
  * Restaurar Backup chama a função `chooseBackupFile()`, desta mesma classe, que listará o nome de cada arquivo backup disponível para ser restaurado e retorna a escolha do usuário para o parâmetro formal `String filename` da função `decodifica(...)`.

## Resultados

### Checklist

#### - Há uma rotina de compactação usando o algoritmo LZW para fazer backup dos arquivos?

<pre> Sim, os arquivos .db são lidos como um vetor de bytes e compactados utilizando-se do algoritmo LZW. <br> <strong>OBS:</strong> Os bits do índice foram fixados em 16bits para o processo de codificação e decodificação. </pre>

#### - Há uma rotina de descompactação usando o algoritmo LZW para recuperação dos arquivos?

<pre> Sim, a descompressão é feita byte a byte, utilizando de um vetor de bytes único contendo todos os arquivos compactados e armazenados em um diretório de restauração específico para melhor legilibidade e usabilidade. </pre>

#### - O usuário pode escolher a versão a recuperar?

<pre> Sim, através da função <i><strong>chooseBackupFile()</strong></i>. </pre>

#### - Qual foi a taxa de compressão alcançada por esse backup?

<pre>O tamanho do backup chegou a ser aproximadamente 1,39 vezes menor do que a soma de todos os arquivos descompactados, considerando os arquivos de dados iniciais. Portanto, a taxa de compressão foi de aproximadamente 139%.</pre>

#### - O trabalho está funcionando corretamente?

<pre> Sim, o trabalho funciona corretamente. </pre>

#### - O trabalho está completo?

<pre> Sim, o trabalho está completo. </pre>

#### - O trabalho é original e não a cópia de um trabalho de um colega?

<pre> Sim, o trabalho foi implemetado usando código desenvolvido pelo grupo no TP2 (Listas Invertidas) e também da base do algoritmo LZW disponibilizada pelo Professor no Canvas. </pre>
