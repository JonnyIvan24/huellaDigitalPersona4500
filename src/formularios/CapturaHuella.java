package formularios;

import bd.ConexionBD;
import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.*;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CapturaHuella extends JFrame{
    private JPanel jPanelPrincipal;
    private JPanel jPanelHuella;
    private JPanel jpanelAcciones;
    private JLabel lblImagenHuella;
    private JPanel jpanelBotones;
    private JPanel jPanelTxtArea;
    private JButton verificarButton;
    private JButton identificarButton;
    private JButton guardarButton;
    private JButton salirButton;
    private JScrollPane txtAreascroll;
    private JTextArea txtArea;

    public CapturaHuella(){
        super("Huellas dactilares");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Imposible modificar el tema visual", "Lookandfeel inválido.",
                    JOptionPane.ERROR_MESSAGE);
        }

        setContentPane(jPanelPrincipal);

        txtArea.setEditable(false);

        salirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        verificarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombre = JOptionPane.showInputDialog("Nombre a verificar:");
                verificarHuella(nombre);
                Reclutador.clear();
            }
        });

        identificarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    identificarHuella();
                    Reclutador.clear();
                } catch (IOException ex) {
                    Logger.getLogger(CapturaHuella.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        guardarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarHuella();
                Reclutador.clear();
                lblImagenHuella.setIcon(null);
                start();
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        Iniciar();
        start();
        EstadoHuellas();
        guardarButton.setEnabled(false);
        identificarButton.setEnabled(false);
        verificarButton.setEnabled(false);
        salirButton.grabFocus();
    }//GEN-LAST:event_formWindowOpened

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        stop();
    }//GEN-LAST:event_formWindowClosing

    //Varible que permite iniciar el dispositivo de lector de huella conectado
    // con sus distintos metodos.
    private DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();

    //Varible que permite establecer las capturas de la huellas, para determina sus caracteristicas
    // y poder estimar la creacion de un template de la huella para luego poder guardarla
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();

    //Esta variable tambien captura una huella del lector y crea sus caracteristcas para auntetificarla
    // o verificarla con alguna guarda en la BD
    private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();

    //Variable que para crear el template de la huella luego de que se hallan creado las caracteriticas
    // necesarias de la huella si no ha ocurrido ningun problema
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";

    protected void Iniciar(){
        Lector.addDataListener(new DPFPDataAdapter() {
            @Override public void dataAcquired(final DPFPDataEvent e) {
                SwingUtilities.invokeLater(new Runnable() {	public void run() {
                    EnviarTexto("La Huella Digital ha sido Capturada");
                    ProcesarCaptura(e.getSample());
                }});}
        });

        Lector.addReaderStatusListener(new DPFPReaderStatusAdapter() {
            @Override public void readerConnected(final DPFPReaderStatusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {	public void run() {
                    EnviarTexto("El Sensor de Huella Digital esta Activado o Conectado");
                }});}
            @Override public void readerDisconnected(final DPFPReaderStatusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {	public void run() {
                    EnviarTexto("El Sensor de Huella Digital esta Desactivado o no Conectado");
                }});}
        });

        Lector.addSensorListener(new DPFPSensorAdapter() {
            @Override public void fingerTouched(final DPFPSensorEvent e) {
                SwingUtilities.invokeLater(new Runnable() {	public void run() {
                    EnviarTexto("El dedo ha sido colocado sobre el Lector de Huella");
                }});}
            @Override public void fingerGone(final DPFPSensorEvent e) {
                SwingUtilities.invokeLater(new Runnable() {	public void run() {
                    EnviarTexto("El dedo ha sido quitado del Lector de Huella");
                }});}
        });

        Lector.addErrorListener(new DPFPErrorAdapter(){
            public void errorReader(final DPFPErrorEvent e){
                SwingUtilities.invokeLater(new Runnable() {  public void run() {
                    EnviarTexto("Error: "+e.getError());
                }});}
        });
    }

    //variables que nos ayudan para crear una nueva huella o para verificarla
    public DPFPFeatureSet featuresinscripcion;
    public DPFPFeatureSet featuresverificacion;


    public  void ProcesarCaptura(DPFPSample sample)
    {
        // Procesar la muestra de la huella y crear un conjunto de características con el propósito de inscripción.
        featuresinscripcion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);

        // Procesar la muestra de la huella y crear un conjunto de características con el propósito de verificacion.
        featuresverificacion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

        // Comprobar la calidad de la muestra de la huella y lo añade a su reclutador si es bueno
        if (featuresinscripcion != null)
            try{
                System.out.println("Las Caracteristicas de la Huella han sido creada");
                Reclutador.addFeatures(featuresinscripcion);// Agregar las caracteristicas de la huella a la plantilla a crear

                // Dibuja la huella dactilar capturada.
                Image image=CrearImagenHuella(sample);
                DibujarHuella(image);

                verificarButton.setEnabled(true);
                identificarButton.setEnabled(true);

            }catch (DPFPImageQualityException ex) {
                System.err.println("Error: "+ex.getMessage());
            }

            finally {
                EstadoHuellas();
                // Comprueba si la plantilla se ha creado.
                switch(Reclutador.getTemplateStatus())
                {
                    case TEMPLATE_STATUS_READY:	// informe de éxito y detiene  la captura de huellas
                        stop();
                        setTemplate(Reclutador.getTemplate());
                        EnviarTexto("La Plantilla de la Huella ha Sido Creada, ya puede Verificarla o Identificarla");
                        identificarButton.setEnabled(false);
                        verificarButton.setEnabled(false);
                        guardarButton.setEnabled(true);
                        guardarButton.grabFocus();
                        break;

                    case TEMPLATE_STATUS_FAILED: // informe de fallas y reiniciar la captura de huellas
                        Reclutador.clear();
                        stop();
                        EstadoHuellas();
                        setTemplate(null);
                        JOptionPane.showMessageDialog(CapturaHuella.this, "La Plantilla de la Huella no pudo ser creada, Repita el Proceso", "Inscripcion de Huellas Dactilares", JOptionPane.ERROR_MESSAGE);
                        start();
                        break;
                }
            }
    }

    public  DPFPFeatureSet extraerCaracteristicas(DPFPSample sample, DPFPDataPurpose purpose){
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            return null;
        }
    }

    public  Image CrearImagenHuella(DPFPSample sample) {
        return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }

    public void DibujarHuella(Image image) {
        lblImagenHuella.setIcon(new ImageIcon(
                image.getScaledInstance(lblImagenHuella.getWidth(), lblImagenHuella.getHeight(), Image.SCALE_DEFAULT)));
        repaint();
    }

    public  void EstadoHuellas(){
        EnviarTexto("Muestra de Huellas Necesarias para Guardar Template "+ Reclutador.getFeaturesNeeded());
    }

    public void EnviarTexto(String string) {
        txtArea.append(string + "\n");
    }

    public  void start(){
        Lector.startCapture();
        EnviarTexto("Utilizando el Lector de Huella Dactilar ");
    }

    public  void stop(){
        Lector.stopCapture();
        EnviarTexto("No se está usando el Lector de Huella Dactilar ");
    }

    private ConexionBD con = new ConexionBD();

    /**
     * Guarda los datos de la huella digital actual en la base de datos
     */
    public void guardarHuella(){
        //Obtiene los datos del template de la huella actual
        ByteArrayInputStream datosHuella = new ByteArrayInputStream(template.serialize());
        Integer tamañoHuella=template.serialize().length;

        //Pregunta el nombre de la persona a la cual corresponde dicha huella
        String nombre = JOptionPane.showInputDialog("Nombre:");
        try {
            //Establece los valores para la sentencia SQL
            Connection c=con.conectar(); //establece la conexion con la BD
            PreparedStatement guardarStmt = c.prepareStatement("INSERT INTO somhue(huenombre, huehuella) values(?,?)");

            guardarStmt.setString(1,nombre);
            guardarStmt.setBinaryStream(2, datosHuella,tamañoHuella);
            //Ejecuta la sentencia
            guardarStmt.execute();
            guardarStmt.close();
            JOptionPane.showMessageDialog(null,"Huella Guardada Correctamente");
            con.desconectar();
            guardarButton.setEnabled(false);
            verificarButton.grabFocus();
        } catch (SQLException ex) {
            //Si ocurre un error lo indica en la consola
            System.err.println("Error al guardar los datos de la huella.");
        }finally{
            con.desconectar();
        }
    }

    /**
     * Verifica la huella digital actual contra otra en la base de datos
    */
    public void verificarHuella(String nom) {
        try {
            //Establece los valores para la sentencia SQL
            Connection c=con.conectar();
            //Obtiene la plantilla correspondiente a la persona indicada
            PreparedStatement verificarStmt = c.prepareStatement("SELECT huehuella FROM somhue WHERE huenombre=?");
            verificarStmt.setString(1,nom);
            ResultSet rs = verificarStmt.executeQuery();

            //Si se encuentra el nombre en la base de datos
            if (rs.next()){
                //Lee la plantilla de la base de datos
                byte templateBuffer[] = rs.getBytes("huehuella");
                //Crea una nueva plantilla a partir de la guardada en la base de datos
                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                //Envia la plantilla creada al objeto contendor de Template del componente de huella digital
                setTemplate(referenceTemplate);

                // Compara las caracteriticas de la huella recientemente capturda con la
                // plantilla guardada al usuario especifico en la base de datos
                DPFPVerificationResult result = Verificador.verify(featuresverificacion, getTemplate());

                //compara las plantilas (actual vs bd)
                if (result.isVerified())
                    JOptionPane.showMessageDialog(null, "Las huella capturada coinciden con la de "+nom,"Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
                else
                    JOptionPane.showMessageDialog(null, "No corresponde la huella con "+nom, "Verificacion de Huella", JOptionPane.ERROR_MESSAGE);

                //Si no encuentra alguna huella correspondiente al nombre lo indica con un mensaje
            } else {
                JOptionPane.showMessageDialog(null, "No existe un registro de huella para "+nom, "Verificacion de Huella", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            //Si ocurre un error lo indica en la consola
            System.err.println("Error al verificar los datos de la huella.");
        }finally{
            con.desconectar();
        }
    }

    /**
     * Identifica a una persona registrada por medio de su huella digital
     */
    public void identificarHuella() throws IOException {
        try {
            //Establece los valores para la sentencia SQL
            Connection c=con.conectar();

            //Obtiene todas las huellas de la bd
            PreparedStatement identificarStmt = c.prepareStatement("SELECT huenombre,huehuella FROM somhue");
            ResultSet rs = identificarStmt.executeQuery();

            //Si se encuentra el nombre en la base de datos
            while(rs.next()){
                //Lee la plantilla de la base de datos
                byte templateBuffer[] = rs.getBytes("huehuella");
                String nombre=rs.getString("huenombre");
                //Crea una nueva plantilla a partir de la guardada en la base de datos
                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                //Envia la plantilla creada al objeto contendor de Template del componente de huella digital
                setTemplate(referenceTemplate);

                // Compara las caracteriticas de la huella recientemente capturda con la
                // alguna plantilla guardada en la base de datos que coincide con ese tipo
                DPFPVerificationResult result = Verificador.verify(featuresverificacion, getTemplate());

                //compara las plantilas (actual vs bd)
                //Si encuentra correspondencia dibuja el mapa
                //e indica el nombre de la persona que coincidió.
                if (result.isVerified()){
                    //crea la imagen de los datos guardado de las huellas guardadas en la base de datos
                    JOptionPane.showMessageDialog(null, "Las huella capturada es de "+nombre,"Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
            //Si no encuentra alguna huella correspondiente al nombre lo indica con un mensaje
            JOptionPane.showMessageDialog(null, "No existe ningún registro que coincida con la huella", "Verificacion de Huella", JOptionPane.ERROR_MESSAGE);
            setTemplate(null);
        } catch (SQLException e) {
            //Si ocurre un error lo indica en la consola
            System.err.println("Error al identificar huella dactilar."+e.getMessage());
        }finally{
            con.desconectar();
        }
    }

    public DPFPTemplate getTemplate() {
        return template;
    }

    public void setTemplate(DPFPTemplate template) {
        DPFPTemplate old = this.template;
        this.template = template;
        firePropertyChange(TEMPLATE_PROPERTY, old, template);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new CapturaHuella();
                frame.setVisible(true);
                frame.setSize(500, 500);
            }
        });
    }
}
