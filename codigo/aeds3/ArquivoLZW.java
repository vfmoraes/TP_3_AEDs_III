package aeds3;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Date;

public class ArquivoLZW {
    public static final int BITS_POR_INDICE = 12;

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

    public void decodifica(String filePath) throws Exception {
        File inputFile = new File(filePath);
        String outputDirPath = "codigo/dados/restored/";
        File outputDir = new File(outputDirPath);

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

            for (int j = -128; j < 128; j++) {
                vetorBytes = new ArrayList<>();
                vetorBytes.add((byte) j);
                dicionario.add(vetorBytes);
            }

            ArrayList<Byte> previousSequence = new ArrayList<>();
            long fileSize = fileIndex == numFiles - 1 ? in.length() : pontosDeParada[fileIndex + 1];

            while (in.getFilePointer() < fileSize) {
                int index = in.readUnsignedShort(); // Reading 12-bit index correctly
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
        String[] nomeArquivos = (new File(dir)).list();
        Date dt = new Date();
        long time = dt.getTime();
        String backupDirPath = "codigo/dados/backups/";

        File backupDir = new File(backupDirPath);
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                throw new Exception("Failed to create directory " + backupDirPath);
            }
        }

        RandomAccessFile out = new RandomAccessFile(backupDirPath + "backup-" + time + ".lzw", "rw");
        RandomAccessFile outNames = new RandomAccessFile(backupDirPath + "backup_names-" + time + ".lzw", "rw");

        out.writeInt(0);
        for (int i = 0; i < nomeArquivos.length; i++) {
            out.writeLong(0);
        }

        for (String arq : nomeArquivos) {
            File fl = new File(dir + arq);
            String[] format = arq.split("\\.");
            ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();
            ArrayList<Byte> vetorBytes;

            byte b;
            for (int j = -128; j < 128; j++) {
                b = (byte) j;
                vetorBytes = new ArrayList<>();
                vetorBytes.add(b);
                dicionario.add(vetorBytes);
            }

            if (fl.isDirectory()) {
                System.out.println("Pulando diretorio - " + dir + arq);
                atualizarPontosDeParada(out);
                continue;
            }

            if (format.length < 2 || !format[format.length - 1].equals("db")) {
                System.out.println("Pulando arquivo - " + dir + arq);
                atualizarPontosDeParada(out);
                continue;
            }

            outNames.writeUTF(arq + "\n");

            RandomAccessFile in = new RandomAccessFile(dir + arq, "r");

            vetorBytes = new ArrayList<>();
            while (in.getFilePointer() != in.length()) {
                byte leitura = in.readByte();
                vetorBytes.add(leitura);
                boolean contains = false;

                for (ArrayList<Byte> arr : dicionario) {
                    contains = true;
                    if (vetorBytes.size() == arr.size()) {
                        for (int i = 0; i < vetorBytes.size(); i++) {
                            if (!arr.get(i).equals(vetorBytes.get(i))) {
                                contains = false;
                                break;
                            }
                        }
                    } else {
                        contains = false;
                    }

                    if (contains) break;
                }

                if (contains) {
                    continue;
                } else {
                    if (dicionario.size() < Math.pow(2, BITS_POR_INDICE)) {
                        dicionario.add(new ArrayList<>(vetorBytes));
                    }
                    vetorBytes.remove(vetorBytes.size() - 1);
                    out.writeShort(dicionario.indexOf(vetorBytes)); // Writing 12-bit index correctly
                    vetorBytes = new ArrayList<>();
                    vetorBytes.add(leitura);
                }
            }

            atualizarPontosDeParada(out);
            in.close();
        }

        out.close();
        outNames.close();
    }
}
