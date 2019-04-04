package formularios;

import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.readers.DPFPReaderDescription;
import com.digitalpersona.onetouch.readers.DPFPReadersCollection;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        listReaders();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame jFrame = new CapturaHuella();
                jFrame.setSize(500,500);
                jFrame.setVisible(true);
            }
        });
    }

    public static  void  listReaders(){
        DPFPReadersCollection readers = DPFPGlobal.getReadersFactory().getReaders();
        if (readers == null || readers.size() == 0){
            System.out.println("no se encontraron lectores");
            return;
        }
        System.out.println("Lectores");
        for (DPFPReaderDescription readerDescription : readers){
            System.out.println(readerDescription.getSerialNumber());
        }
    }
}
