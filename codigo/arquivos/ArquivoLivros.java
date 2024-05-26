package arquivos;

import aeds3.Arquivo;
import aeds3.ArvoreBMais;
import aeds3.HashExtensivel;
import aeds3.ParIntInt;
import aeds3.ListaInvertida;
import entidades.Livro;

import java.text.Normalizer;
import java.util.ArrayList;
import java.io.File;
import java.io.RandomAccessFile;

public class ArquivoLivros extends Arquivo<Livro> {
  ArrayList<String> stopWords_list;
  ListaInvertida listaTitulos;
  HashExtensivel<ParIsbnId> indiceIndiretoISBN;
  ArvoreBMais<ParIntInt> relLivrosDaCategoria;


  public ArquivoLivros() throws Exception {
    super("livros", Livro.class.getConstructor());
    indiceIndiretoISBN = new HashExtensivel<>(
        ParIsbnId.class.getConstructor(),
        4,
        "dados/livros_isbn.hash_d.db",
        "dados/livros_isbn.hash_c.db");
    relLivrosDaCategoria = new ArvoreBMais<>(ParIntInt.class.getConstructor(), 4, "dados/livros_categorias.btree.db");
    //Criação da lista invertida
    listaTitulos = new ListaInvertida(10, "dados/dicionario.listainv.db", "dados/blocos.listainv.db");

    //Criação da lista de stopwords
    stopWords_list = new ArrayList<String>();
    File FL = new File("dados/stopwords.txt");
    RandomAccessFile RF = new RandomAccessFile(FL, "rw");
    while(RF.getFilePointer() < RF.length()) {
      stopWords_list.add(RF.readLine()); 
    }
    // for(String s : stopWords_list){ 
    //   System.out.println("\n"+s+": "+s.length() );
    //   for(int i=0; i< s.length(); i++){
    //     System.out.println("Char ["+ i + "] = " + (int)s.charAt(i));
    //   }
    // }

    RF.close();
  }

  //Função para descobrir se o arraylist possui a String
  boolean isStopWord(String s){
    return stopWords_list.contains(s);
  }

  String tratarString(String s){
    //remover caracteres especiais
    for (int i = 33; i < 65; i++) {
      s = s.replace("" + (char) i, "");
    }

    //tratando string titulo
    s = Normalizer.normalize(s, Normalizer.Form.NFD); 
    s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    s = s.toLowerCase();

    return s;
  }

  @Override
  public int create(Livro obj) throws Exception {
    int id = super.create(obj);
    obj.setID(id);
    indiceIndiretoISBN.create(new ParIsbnId(obj.getIsbn(), obj.getID()));
    relLivrosDaCategoria.create(new ParIntInt(obj.getIdCategoria(), obj.getID()));
    
    //---tratar string---//
    String tratada = tratarString(obj.getTitulo()); 
    
    //---criar lista invertida---//
    String[] splitted = tratada.split(" ");
    for(int i=0; i<splitted.length; i++){
      if(!isStopWord(splitted[i])){
        listaTitulos.create(splitted[i], id);
      }
    }

    return id;
  }

  public Livro[] read(String titulo) throws Exception {
    int posInicial = 0;
    int[] IDs;
    String[] splitted;
    Livro[] livro;

    //tratar titulo
    titulo = tratarString(titulo); 

    splitted = titulo.split(" ");
    if(splitted.length == 0) throw new Exception("Nome do livro vazio");
    while(posInicial<splitted.length && isStopWord(splitted[posInicial++]));
    IDs = listaTitulos.read(splitted[posInicial-1]);

    //Encontrar interceção entre toda a lista de Títulos
    for(int i = posInicial; i<splitted.length; i++){
      //Se existir IDs continua a verificação
      if(IDs.length > 0 && !isStopWord(splitted[i])){
        ArrayList<Integer> result = new ArrayList<Integer>();
        int[] IDs_tmp = listaTitulos.read(splitted[i]);
        
        //Encontrar interceção entre IDs_tmp e IDs, armazenando em result
        for(int j=0; j<IDs.length; j++){
          boolean existeIntercessao = false;
          for(int k=0; k<IDs_tmp.length; k++){
            if(IDs[j] == IDs_tmp[k]){
              existeIntercessao = true;
              k = IDs_tmp.length;
            }
          }
          if(existeIntercessao){
            result.add(IDs[j]);
          }
        }

        //Atualizar os IDs com os valores do arraylist result
        IDs = new int[result.size()];
        for(int j=0; j<result.size(); j++)
          IDs[j] = result.get(j);
        
      //Se IDs estiver vazio encerra execução do for
      } else if (IDs.length == 0){
        i=splitted.length;
      }
    }

    livro = new Livro[IDs.length];
    for(int i=0; i<IDs.length; i++){
      livro[i] = super.read(IDs[i]);
    }

    return livro;
  }

  public Livro readISBN(String isbn) throws Exception {
    ParIsbnId pii = indiceIndiretoISBN.read(ParIsbnId.hashIsbn(isbn));
    if (pii == null)
      return null;
    int id = pii.getId();
    return super.read(id);
  }

  @Override
  public boolean delete(int id) throws Exception {
    Livro obj = super.read(id);
    if (obj != null)
      if (indiceIndiretoISBN.delete(ParIsbnId.hashIsbn(obj.getIsbn()))
          &&
          relLivrosDaCategoria.delete(new ParIntInt(obj.getIdCategoria(), obj.getID()))){
        
        String titulo = obj.getTitulo();
        //tratando string titulo
        titulo = tratarString(titulo); 
        String[] splitted = titulo.split(" ");
        //repetição para deletar o Título do dicionario de Títulos
        for(int i=0; i<splitted.length; i++){
          if(!isStopWord(splitted[i]))
            listaTitulos.delete(splitted[i], id);
          else 
            System.out.println("StopWord");
        }
        return super.delete(id);
      }
    return false;
  }

  @Override
  public boolean update(Livro novoLivro) throws Exception {
    Livro livroAntigo = super.read(novoLivro.getID());
    if (livroAntigo != null) {

      // Testa alteração do ISBN
      if (livroAntigo.getIsbn().compareTo(novoLivro.getIsbn()) != 0) {
        indiceIndiretoISBN.delete(ParIsbnId.hashIsbn(livroAntigo.getIsbn()));
        indiceIndiretoISBN.create(new ParIsbnId(novoLivro.getIsbn(), novoLivro.getID()));
      }

      // Testa alteração da categoria
      if (livroAntigo.getIdCategoria() != novoLivro.getIdCategoria()) {
        relLivrosDaCategoria.delete(new ParIntInt(livroAntigo.getIdCategoria(), livroAntigo.getID()));
        relLivrosDaCategoria.create(new ParIntInt(novoLivro.getIdCategoria(), novoLivro.getID()));
      }


      String[] tituloAntigo = ( tratarString(livroAntigo.getTitulo()) ).split(" ");
      String[] tituloNovo = ( tratarString(novoLivro.getTitulo()) ).split(" ");

      for(int i=0; i<tituloAntigo.length; i++)
          listaTitulos.delete(tituloAntigo[i], livroAntigo.getID());
      for(int i=0; i<tituloNovo.length; i++)
          listaTitulos.create(tituloNovo[i], novoLivro.getID());

      boolean status = super.update(novoLivro);

      // Atualiza o livro
      return status;
    }
    return false;
  }
}
