package aeds3;

import aeds3.LZW;
import java.io.RandomAccessFile;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.io.*;
import java.util.*;

public class ArquivoLZW{
    public static final int BITS_POR_INDICE = 12;

    public void atualizarPontosDeParada(RandomAccessFile out) throws Exception{
        long atual = out.getFilePointer();
        out.seek(0);
        int qtde = out.readInt();
        out.seek(0);
        out.writeInt(qtde+1);
        out.seek(qtde*8);
        out.writeLong(atual);
        out.seek(atual);
    }

    // public Byte[] read(String dir){
        
    // }

    public void decodifica(String filePath) throws Exception {
        File inputFile = new File(filePath);
        String outputDirPath = "codigo/dados/restored/";
        File outputDir = new File(outputDirPath);

        // Verifica e cria o diretório de saída se não existir
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new Exception("Failed to create directory " + outputDirPath);
            }
        }

        RandomAccessFile in = new RandomAccessFile(inputFile, "r");
        
        String backupNamesPath = filePath.replace("backup-", "backup_names-");
        RandomAccessFile inNames = new RandomAccessFile(backupNamesPath, "r");

        int numFiles = in.readInt();
        long[] pontosDeParada = new long[numFiles];
        for (int i = 0; i < numFiles; i++) {
            pontosDeParada[i] = in.readLong();
        }

        for (int fileIndex = 0; fileIndex < numFiles; fileIndex++) {
            String fileName = inNames.readUTF().trim();
            File outputFile = new File(outputDirPath + fileName);
            RandomAccessFile out = new RandomAccessFile(outputFile, "rw");

            ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();
            ArrayList<Byte> vetorBytes;

            // Inicializa o dicionário
            for (int j = -128; j < 128; j++) {
                vetorBytes = new ArrayList<>();
                vetorBytes.add((byte) j);
                dicionario.add(vetorBytes);
            }

            ArrayList<Byte> previousSequence = new ArrayList<>();
            long fileSize = fileIndex == numFiles - 1 ? in.length() : pontosDeParada[fileIndex + 1];

            while (in.getFilePointer() < fileSize) {
                int index = in.readUnsignedByte();
                ArrayList<Byte> currentSequence = new ArrayList<>(dicionario.get(index));
                for (byte b : currentSequence) {
                    out.write(b);
                }

                if (!previousSequence.isEmpty()) {
                    previousSequence.add(currentSequence.get(0));
                    if (dicionario.size() < Math.pow(2, BITS_POR_INDICE)) {
                        dicionario.add(new ArrayList<>(previousSequence));
                    }
                }

                previousSequence = new ArrayList<>(currentSequence);
            }

            out.close();
        }

        in.close();
        inNames.close();
    }

    public void codifica(String dir) throws Exception {
        String[] nomeArquivos = (new File(dir)).list(); //pegar lista dos arquivos a serem codificados do diretório
        // long[] pontoDeParada = new long[nomeArquivos.length];
        Date dt = new Date();
        long time = dt.getTime();
        String backupDirPath = "codigo/dados/backups/";
        

        // Verifica e cria o diretório de backup se não existir
        File backupDir = new File(backupDirPath);
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                throw new Exception("Failed to create directory " + backupDirPath);
            }
        }

        RandomAccessFile out = new RandomAccessFile(backupDirPath+"backup-"+time+".lzw", "rw");
        RandomAccessFile outNames = new RandomAccessFile(backupDirPath+"backup_names-"+time+".lzw", "rw");
        
        

        //                    long(8 bytes)+int(4 bytes)
        //pular bytes dos pontos de parada + tamanho (quantidade de pontos)
        // for(int i=0; i<nomeArquivos.length*8 + 4; i++){
        //     out.writeByte(0);
        // }
        out.writeInt(0);
        for(int i=0; i<nomeArquivos.length; i++){
            out.writeLong(0);
        }

        //Realizar a codificação de todos os arquivos
        for(String arq : nomeArquivos){
            File fl = new File(dir + arq);
            String[] format = arq.split("\\.");
            ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>(); // dicionario
            ArrayList<Byte> vetorBytes;  // auxiliar para cada elemento do dicionario
            // inicializa o dicionário
            byte b;
            for(int j=-128; j<128; j++) {
                b = (byte)j;
                vetorBytes = new ArrayList<>();
                vetorBytes.add(b);
                dicionario.add(vetorBytes);
            }

            
            
            //verificação para pular diretórios
            if(fl.isDirectory()){
                System.out.println("Pulando diretorio - " + dir + arq);
                //Colocar um ponto de parada igual ao anterior para não perder a referência
                atualizarPontosDeParada(out);
                continue;
            }
            //verificação para pular arquivos que não forem .db
            
            if(format.length<2 || !format[format.length - 1].equals("db")){
                System.out.println("Pulando arquivo - " + dir + arq);
                
                //Colocar um ponto de parada igual ao anterior para não perder a referência
                atualizarPontosDeParada(out);
                continue;
            }

            //Se o arquivo for um .db que não é um diretório então armazenar no arquivo de nomes
            outNames.writeUTF(arq+"\n");
            

            RandomAccessFile in = new RandomAccessFile(dir + arq, "r");

            vetorBytes = new ArrayList<>(); //iniciar auxiliar para leitura 
            while(in.getFilePointer() != in.length()){
                byte leitura = in.readByte();
                vetorBytes.add(leitura);
                boolean contains = false;

                //para cada item no dicionário, verificar se ele corresponde à leitura
                for(ArrayList<Byte> arr : dicionario){
                    contains = true;
                    if(vetorBytes.size() == arr.size()){ 
                        for(int i = 0; i<vetorBytes.size(); i++){
                            if(arr.get(i) != vetorBytes.get(i)){
                                contains = false;
                                i = vetorBytes.size();
                            }
                        }
                    } else {
                        contains = false;
                    }

                    if(contains) 
                        break;
                }

                
                if(contains){
                // if(dicionario.contains(vetorBytes)){
                    continue; //Se contém o vetor de bytes verifica o próximo
                } else {
                    if(dicionario.size() < Math.pow(2, BITS_POR_INDICE)){
                        dicionario.add(vetorBytes); 
                    }
                    vetorBytes.removeLast(); //remove o ultimo lido para buscar dicionario corretamente
                    out.writeByte(dicionario.indexOf(vetorBytes)); //escreve indice no arquivo final após a codificação
                    vetorBytes = new ArrayList<>();
                    vetorBytes.add(leitura); 
                }
            }
            
            //colocar ponto de parada para o arquivo atual
            atualizarPontosDeParada(out);

            in.close();
        }//fim for-each
        out.close();
        outNames.close();
    }
}
