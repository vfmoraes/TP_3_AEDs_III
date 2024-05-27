package aeds3;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class LZW {

    public static final int BITS_POR_INDICE = 12;

        public static void decodifica(String filePath) throws Exception {
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
        String backupNamesPath = filePath.replace(".lzw", "_names.lzw");
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


    public static byte[] codifica(byte[] msgBytes) throws Exception {
        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>(); // dicionario
        ArrayList<Byte> vetorBytes;  // auxiliar para cada elemento do dicionario
        ArrayList<Integer> saida = new ArrayList<>();

        // inicializa o dicionário
        byte b;
        for(int j=-128; j<128; j++) {
            b = (byte)j;
            vetorBytes = new ArrayList<>();
            vetorBytes.add(b);
            dicionario.add(vetorBytes);
        }

        int i=0;
        int indice=-1;
        int ultimoIndice;
        while(indice==-1 && i<msgBytes.length) { // testa se o último vetor de bytes não parou no meio caminho por falta de bytes
            vetorBytes = new ArrayList<>();
            b = msgBytes[i];
            vetorBytes.add(b);
            indice = dicionario.indexOf(vetorBytes);
            ultimoIndice = indice;

            while(indice!=-1 && i<msgBytes.length-1) {
                i++;
                b = msgBytes[i];
                vetorBytes.add(b);
                ultimoIndice = indice;
                indice = dicionario.indexOf(vetorBytes);

            }

            // acrescenta o último índice à saída
            saida.add(ultimoIndice);

            // acrescenta o novo vetor de bytes ao dicionário
            if(dicionario.size() < (Math.pow(2, BITS_POR_INDICE))) {
                dicionario.add(vetorBytes);
            }

        }

        System.out.println("Indices");
        System.out.println(saida);
        System.out.println("Dicionário tem "+dicionario.size()+" elementos");
        
        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        for(i=0; i<saida.size(); i++) {
            bs.add(saida.get(i));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(bs.size());
        dos.write(bs.getBytes());
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static byte[] decodifica(byte[] msgCodificada) throws Exception {

        ByteArrayInputStream bais = new ByteArrayInputStream(msgCodificada);
        DataInputStream dis = new DataInputStream(bais);
        int n = dis.readInt();
        byte[] bytes = new byte[msgCodificada.length-4];
        dis.read(bytes);
        BitSequence bs = new BitSequence(BITS_POR_INDICE);
        bs.setBytes(n, bytes);

        // Recupera os números do bitset
        ArrayList<Integer> entrada = new ArrayList<>();
        int i, j;
        for(i=0; i<bs.size(); i++) {
            j = bs.get(i);
            entrada.add(j);
        }

        // inicializa o dicionário
        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>(); // dicionario
        ArrayList<Byte> vetorBytes;  // auxiliar para cada elemento do dicionario
        byte b;
        for(j=-128; j<128; j++) {
            b = (byte)j;
            vetorBytes = new ArrayList<>();
            vetorBytes.add(b);
            dicionario.add(vetorBytes);
        }

        // Decodifica os números
        ArrayList<Byte> proximoVetorBytes;
        ArrayList<Byte> msgDecodificada = new ArrayList<>();
        i = 0;
        while( i< entrada.size() ) {

            // decodifica o número
            vetorBytes = (ArrayList<Byte>)(dicionario.get(entrada.get(i)).clone());
            msgDecodificada.addAll(vetorBytes);

            // decodifica o próximo número
            i++;
            if(i<entrada.size()) {
                proximoVetorBytes = dicionario.get(entrada.get(i));
                vetorBytes.add(proximoVetorBytes.get(0));

                // adiciona o vetor de bytes (+1 byte do próximo vetor) ao fim do dicionário
                if(dicionario.size()<Math.pow(2,BITS_POR_INDICE))
                    dicionario.add(vetorBytes);
            }

        }

        byte[] msgDecodificadaBytes = new byte[msgDecodificada.size()];
        for(i=0; i<msgDecodificada.size(); i++)
            msgDecodificadaBytes[i] = msgDecodificada.get(i);
        return msgDecodificadaBytes;

    }
}