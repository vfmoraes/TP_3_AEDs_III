import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import aeds3.ArquivoLZW;

public class MenuBackup {
    private static Scanner console = new Scanner(System.in);

    // -------------
    // Diretorios de dados e complementos
    // -------------
    private static String initialDataPath = "codigo/dados/";
    private static String restoredComplementPath = "restored/";
    private static String backupComplementPath = "backups/";

    // ---------------------
    // MENU_BACKUP
    // ---------------------
    public String chooseBackupFile() throws Exception {
        String backupPath = initialDataPath + "backups";
        File dir = new File(backupPath);
        String escolha = "";

        // Verificar se o diretório existe e é de fato um diretório
        if (dir.exists() && dir.isDirectory()) {
            // Utilizando FilenameFilter para filtrar os arquivos que começam com "backup-"
            String[] filteredFiles = dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("backup-");
                }
            });
            // Imprimindo os arquivos filtrados
            if (filteredFiles != null) {
                System.out.println("\n\n\nBOOKAEDS 1.0");
                System.out.println("------------");
                System.out.println("\n> Início > Backup > Restauração");

                System.out.println("\nEscolha o número referente ao arquivo que deseja fazer backup: ");
                // for (String fileName : filteredFiles) {
                for (int i = 0; i < filteredFiles.length; i++) {
                    System.out.println(i + 1 + ") " + filteredFiles[i]);
                }
                int option;
                System.out.print("\nOpção: ");
                try {
                    option = Integer.valueOf(console.nextLine()) - 1;
                } catch (NumberFormatException e) {
                    option = -1;
                }
                if (option >= 0 && option < filteredFiles.length) {
                    escolha = filteredFiles[option];
                    System.out.println("\nIniciando restauração de " + escolha);
                } else {
                    throw new Exception("Opção inválida");
                }
            } else {
                throw new Exception("Nenhum arquivo encontrado com o prefixo 'backup-'.");
            }
        } else {
            throw new Exception("Diretório não existe ou não é um diretório válido.");
        }

        return escolha;
    }

    public void menuRestaurar() throws Exception {
        File dirRestored = new File(initialDataPath + restoredComplementPath);
        if (!dirRestored.exists() || !dirRestored.isDirectory()) {
            System.out.println("Diretorio de restauração vazio");
        } else {
            String[] nomeArquivos = dirRestored.list();
            ArrayList<Integer> escolhas = new ArrayList<Integer>();
            
            int opcao = -1;
            int i = 0;
            System.out.println("\n\n\nEscolha os arquivos que deseja substituir no banco de dados :");
            for (i = 0; i < nomeArquivos.length; i++) {
                System.out.println((i + 1) + ") " + nomeArquivos[i]);
            }
            System.out.println("\n" + (i + 1) + ") Fazer backup de todos os arquivos");
            System.out.println("\n0) Finalizar escolhas");
            System.out.println("\n" + "Selecione uma opção por vez");
            
            while (opcao != 0) {
                System.out.print("\nOpção: ");
                try {
                    opcao = Integer.valueOf(console.nextLine());
                    
                    if (opcao < 0 || opcao > nomeArquivos.length + 1) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {

                    opcao = -1;
                }

                if (opcao == 0) {
                    break;
                } else if (opcao == nomeArquivos.length + 1) {
                    for (int j = 0; j < nomeArquivos.length; j++) {
                        if (!escolhas.contains(j)) {
                            escolhas.add(j);
                        }
                    }
                    opcao = 0;
                } else if (opcao > 0 && opcao <= nomeArquivos.length) {
                    int index = opcao - 1;
                    if (!escolhas.contains(index)) {
                        escolhas.add(index);
                    } else {
                        System.out.println("Arquivo já selecionado.");
                    }
                } else {
                    System.out.println("Valor inválido");
                }
            }

            for (int j = 0; j < escolhas.size(); j++) {
                substituiArquivo(nomeArquivos[escolhas.get(j)]);
            }
            System.out.println(escolhas.size() == 0 ? "\nNenhum arquivo substituido" : escolhas.size() == nomeArquivos.length ? "\nTodos arquivos substituidos" : "\nArquivos substituidos.");
            System.out.println("\nObs: Caso necessário, todos arquivos restaurados se encontram no diretório \""+initialDataPath+restoredComplementPath+"\"");
        }
    }

    public void substituiArquivo(String file) throws Exception {
        File arquivoOld = new File(initialDataPath + file);
        Path backup = Paths.get(initialDataPath + restoredComplementPath + file);
        Path dados = Paths.get(initialDataPath + file);

        if(!arquivoOld.exists()){
            arquivoOld.createNewFile();
        }

        if (arquivoOld.exists()) {
            arquivoOld.delete();
            Files.copy(backup, dados);
        }

    }

    public void menu() throws Exception {
        // Mostra o menu
        int opcao;
        do {
            System.out.println("\n\n\nBOOKAEDS 1.0");
            System.out.println("------------");
            System.out.println("\n> Início > Backup");
            System.out.println("\n1) Fazer backup");
            System.out.println("2) Restaurar backup");
            System.out.println("\n0) Retornar ao menu anterior");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch (NumberFormatException e) {
                opcao = -1;
            }

            // Seleciona a operação
            switch (opcao) {
                case 1:
                    (new ArquivoLZW()).codifica(initialDataPath, backupComplementPath);
                    break;
                case 2:
                    (new ArquivoLZW()).decodifica(chooseBackupFile(), initialDataPath + backupComplementPath,
                            initialDataPath + restoredComplementPath);
                    menuRestaurar();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 0);
    }
}
