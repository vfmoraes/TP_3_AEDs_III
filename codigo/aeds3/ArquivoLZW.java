package aeds3;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ArquivoLZW {
    public static final int BITS_POR_INDICE = 16;
    

    public void atualizarPontosDeParada(RandomAccessFile out) throws Exception {
        long atual = out.getFilePointer();
        out.seek(0);
        int qtde = out.readInt();
        out.seek(0);
        out.writeInt(qtde + 1);
        out.seek(4 + qtde * 8);
        out.writeLong(atual);
        out.seek(atual);
    }


    public void decodifica(String fileName, String backupDirPath, String restoredPath) throws Exception {
        ArrayList<Long> posParadaArquivos = new ArrayList<Long>();
        ArrayList<String> nomesArquivos = new ArrayList<String>();
        
    
        File outputDir = new File(restoredPath);
        File testFileBackup = new File(backupDirPath+fileName);
        File testFileNames = new File(backupDirPath+fileName.replace("backup", "backup_names"));

        //verificar se o arquivo de backup existe
        if(!testFileBackup.exists()){
            throw new Exception("Arquivo de backup não existe: \"" + backupDirPath + fileName+ "\"");
        } else if(testFileBackup.isDirectory()){
            throw new Exception("Um diretório não pode ser decodificado, escolha um arquivo: \"" + backupDirPath + fileName+ "\"");
        }

        //verificar se o arquivo backup-names existe
        if(!testFileNames.exists()){
            testFileBackup.delete();
            throw new Exception("Backup quebrado - arquivo de nomes não existe: \"" + backupDirPath + fileName + "\nArquivo de backups removido: " + backupDirPath + fileName+ "\"");
        } else if(testFileNames.isDirectory()){
            throw new Exception("Backup quebrado - arquivo de nomes é um diretório: \"" + backupDirPath + fileName+ "\"");
        }

        //criar diretório de saida
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new Exception("Falha ao criar diretório de restauração \'" + backupDirPath+ "\"");
            }
        }

        RandomAccessFile inFileBackup = new RandomAccessFile(backupDirPath+fileName,"r");
        RandomAccessFile inFileNames = new RandomAccessFile(backupDirPath+fileName.replace("backup", "backup_names"), "rw");

        //ler e armazenar todos os pontos de parada e respectivo nome no arquivo
        int qtdeArqBackup = inFileBackup.readInt();
        for(int i=0; i<qtdeArqBackup; i++){
            posParadaArquivos.add(inFileBackup.readLong());
        }

        while(inFileNames.getFilePointer() != inFileNames.length()){
            nomesArquivos.add(inFileNames.readUTF().replace("\n", ""));
        }
        inFileNames.close();
        
        //ler todo arquivo de backup a partir dos pontos de parada
        //long pntParada : posParadaArquivos
        int posArrayNomes = 0;
        for(int i=0; i<posParadaArquivos.size(); i++){
            long pntParada = posParadaArquivos.get(i);
            //se o ponto de parada for igual ao ponteiro atual do arquivo, na codificação ele encontrou um diretorio ou arquivo que não é .db, sendo assim deve pular essa execução
            if(inFileBackup.getFilePointer() == pntParada){
                System.out.println("Pulando linha - na codificação houve algum arquivo incompatível no ponto "+ i + "\n");
                continue;
            }

            //se o ponto de parada for diferente então será feita a decodificação
            String nomeRestaurado = nomesArquivos.get(posArrayNomes++);
            System.out.print("Decodificando arquivo - \"" + nomeRestaurado + "\"" + ". Ponto inicial/final de leitura - " + inFileBackup.getFilePointer() +" / "+ pntParada + "\n");

            //se o arquivo existir, deletar para reescrever o backup
            File testFile = new File(restoredPath + nomeRestaurado);            
            if(testFile.exists()){
                testFile.delete();
            }

            RandomAccessFile outputFile = new RandomAccessFile(restoredPath + nomeRestaurado, "rw");
            
            //inicializar dicionário para decodificação
            ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();
            ArrayList<Byte> vetorBytes;

            for (int j = -128; j < 128; j++) {
                vetorBytes = new ArrayList<>();
                vetorBytes.add((byte) j);
                dicionario.add(vetorBytes);
            }

            ArrayList<Byte> previousSequence = new ArrayList<>();

            
            //ir até o ponto de parada do arquivo
            while(inFileBackup.getFilePointer() != pntParada){
                //ler index do arquivo
                int index = inFileBackup.readShort(); // Leitura do index de 16bits
                ArrayList<Byte> currentSequence;
                //se o número lido for igual ao tamanho do dicionário adiciona o ultimo do anterior
                if(index == dicionario.size()){
                    //Nesse caso a sequencia atual é a mesma sequência lida
                    previousSequence.add(previousSequence.getLast());
                    currentSequence = previousSequence;
                    if (dicionario.size() < Math.pow(2, BITS_POR_INDICE)) {
                        dicionario.add(new ArrayList<>(previousSequence));
                    }
                } else {
                    //identificar index no dicionario e escrever a sequencia decodificada no arquivo de saída
                    currentSequence = new ArrayList<>(dicionario.get(index));
                    //Se tiver elemento na sequencia anterior, adicionar o primeiro byte da sequencia atual para complementar o dicionario
                    if (!previousSequence.isEmpty()) {
                        previousSequence.add(currentSequence.get(0));
                        if (dicionario.size() < Math.pow(2, BITS_POR_INDICE)) {
                            dicionario.add(new ArrayList<>(previousSequence));
                        }
                    }
                }

                //imprimir sequencia atual no arquivo de saída
                for (byte b : currentSequence) {
                    outputFile.write(b);
                    // System.out.print(b);
                }
                previousSequence = new ArrayList<>(currentSequence);
            }
            
            outputFile.close();
        }

        
        inFileBackup.close();



    }

    public void codifica(String dir, String backupComplement) throws Exception {
        String[] nomeArquivos = (new File(dir)).list(); //pegando todos os nomes existentes no diretorio
        
        //Pegar data atual para ser utilizada no arquivo de backup
        Date dt = new Date();

        // Formatar a data no formato desejado
        String dataFormatada = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(dt);
        String backupDirPath = dir + backupComplement;

        //criar diretorio de backup se não existir
        File backupDir = new File(backupDirPath);
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                throw new Exception("Falha ao criar diretório de backups:  \"" + backupDirPath + "\"");
            }
        }


        RandomAccessFile out = new RandomAccessFile(backupDirPath + "backup-" + dataFormatada + ".lzw", "rw");
        RandomAccessFile outNames = new RandomAccessFile(backupDirPath + "backup_names-" + dataFormatada + ".lzw", "rw");

        //pular no arquivo de saída do LZW o inteiro e os longs que representam até onde cada arquivo foi codificado
        out.writeInt(0);
        for (int i = 0; i < nomeArquivos.length; i++) {
            out.writeLong(0);
        }

        long qtdeTotalLinhas = 0;
        long inicioCompressao = out.getFilePointer();
        //para cada arquivo no conjunto de string nomeArquivos, realizar a codificação separada e adicionar no arquivo de saída
        for (String arq : nomeArquivos) {
            File fl = new File(dir + arq);
            String[] format = arq.split("\\.");
            ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();
            ArrayList<Byte> vetorBytes;

            //Se dir/arq for um diretório, pular a codificação dele e atualizar o ponto de parada (que vai ser o ponto inicial)
            if (fl.isDirectory()) {
                System.out.println("Pulando diretorio - \"" + dir + arq + "\"");
                atualizarPontosDeParada(out);
                continue;
            }

            //Se dir/arq não for do tipo .db, pular a codificação dele e atualizar o ponto de parada (que vai ser o ponto inicial)
            if (format.length < 2 || !format[format.length - 1].equals("db")) {
                System.out.println("Pulando arquivo - \"" + dir + arq+ "\"");
                atualizarPontosDeParada(out);
                continue;
            }

            //Apartir desse ponto prosseguir se for um arquivo.db
            System.out.print("Fazendo backup do arquivo - \"" + dir + arq+ "\"");
            long posInicial = out.getFilePointer();//apenas para imprimir no final

            //Esrever os nomes dos arquivos
            outNames.writeUTF(arq);

            //inicializando dicionário
            byte b;
            for (int j = -128; j < 128; j++) {
                b = (byte) j;
                vetorBytes = new ArrayList<>();
                vetorBytes.add(b);
                dicionario.add(vetorBytes);
            }

            
            //abrir arquivo para leitura
            RandomAccessFile in = new RandomAccessFile(dir + arq, "r");
            qtdeTotalLinhas += in.length();

            vetorBytes = new ArrayList<>();//inicializar vetor auxiliar
            while (in.getFilePointer() != in.length()) {//para todo o arquivo de leitura
                byte leitura = in.readByte();
                //adicionar byte atual no vetor auxiliar de bytes
                vetorBytes.add(leitura);
                boolean contains = false;

                //verificar se o vetor de bytes lido está contido no dicionario
                for (ArrayList<Byte> arr : dicionario) {
                    contains = true;
                    if (vetorBytes.size() == arr.size()) { //se possui mesmo tamanho que arr verificar byte a byte
                        for (int i = 0; i < vetorBytes.size(); i++) {
                            if (!arr.get(i).equals(vetorBytes.get(i))) {
                                contains = false;
                                break;
                            }
                        }
                    } else { //senão não contém
                        contains = false;
                    }

                    if (contains) break;
                }

                //se está contido verificar com a próxima leitura
                if (contains) {
                    continue;
                //se não está contido adicionar no dicionário e escrever o index do vetor de Bytes correto
                } else {
                    if (dicionario.size() < Math.pow(2, BITS_POR_INDICE)) {
                        dicionario.add(new ArrayList<>(vetorBytes));
                    }
                    vetorBytes.remove(vetorBytes.size() - 1); //remover o ultimo byte que não contém no dicionario

                    out.writeShort(dicionario.indexOf(vetorBytes)); // Escrevendo index de 16-bit no arquivo de saída
                    
                    vetorBytes = new ArrayList<>();
                    vetorBytes.add(leitura);
                }
            }

            //escrever ponto de parada para o arquivo lido
            atualizarPontosDeParada(out);
            System.out.print(". Posição inicial/final do arquivo em backups - " + posInicial + "/" + out.getFilePointer() + "\n");
            float razao = (float) in.length() / (out.getFilePointer() - posInicial);
            System.out.println("Taxa de compressão do arquivo " + razao*100 + "%");
            in.close();
        }

        long fimCompressao = out.getFilePointer();
        float razao = (float) qtdeTotalLinhas / (fimCompressao-inicioCompressao);
        System.out.println("\nTaxa de compressão do arquivo final " + razao*100 + "%");

        out.close();
        outNames.close();
    }
}