import java.util.Scanner;
import java.io.File;
import java.io.FilenameFilter;
import aeds3.ArquivoLZW;

public class MenuBackup {
    private static Scanner console = new Scanner(System.in);

    //-------------
    // Diretorios de dados e complementos 
    //-------------
    private static String initialDataPath = "codigo/dados/";
    private static String restoredComplementPath = "restored/";
    private static String backupComplementPath = "backups/";

    // ---------------------
    // MENU_BACKUP
    // ---------------------
    public String choseBackupFile() throws Exception{
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
                //for (String fileName : filteredFiles) {
                for(int i=0; i<filteredFiles.length; i++){
                    System.out.println(i+1 + ") " + filteredFiles[i]);
                }
                int option;
                System.out.print("\nOpção: ");
                try {
                    option = Integer.valueOf(console.nextLine())-1;
                } catch (NumberFormatException e) {
                    option = -1;
                }
                if(option >= 0 && option < filteredFiles.length){
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

    public void menu() throws Exception{
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
                    (new ArquivoLZW()).decodifica(choseBackupFile(), initialDataPath + backupComplementPath, initialDataPath + restoredComplementPath);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 0);
    }
}
