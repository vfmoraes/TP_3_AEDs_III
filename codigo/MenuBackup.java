import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import aeds3.ArquivoLZW;

public class MenuBackup {
    private static Scanner console = new Scanner(System.in);


    // ---------------------
    // MENU_BACKUP
    // ---------------------
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
                    (new ArquivoLZW()).codifica("codigo/dados/");
                    break;
                case 2:
                    (new ArquivoLZW()).decodifica("codigo/dados/backups/backup-1716775483638.lzw");
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        } while (opcao != 0);
    }
}
