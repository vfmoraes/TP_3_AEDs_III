package aeds3;
import java.io.RandomAccessFile;
import java.lang.Exception;

public interface RegistroArquivoLZW {
    public byte[] read(RandomAccessFile arq); // ler arquivos
    
    public RandomAccessFile create(String arq, byte[] bytes); //criar arquivo com vetor de bytes

    public void insert(RandomAccessFile arq, byte[] bytes); //Adicionar ao arquivo com vetor de bytes
    
    public void codifica(String dir) throws Exception;
 
    public void decodifica(String arq) throws Exception;
} 
    






// import java.io.IOException;

// public interface RegistroArvoreBMais<T> {

//   public short size(); // tamanho FIXO do registro

//   public byte[] toByteArray() throws IOException; // representação do elemento em um vetor de bytes

//   public void fromByteArray(byte[] ba) throws IOException; // vetor de bytes a ser usado na construção do elemento

//   public int compareTo(T obj); // compara dois elementos

//   public T clone(); // clonagem de objetos

// }