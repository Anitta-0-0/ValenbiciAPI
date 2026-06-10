package es.gva.edu.iesjuandegaray.bicis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ConexionBDD extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textFieldNEstaciones;

	// Declaramos las conexiones y variables estáticas
	private static Connection con; 
	private static Statement s;
	private static DatosJSon dJSon;	
	private static int numEst = 3;
	private static JTextArea textAreaDatos;
	private static JLabel lblNewLabel;
	private static JLabel lblNewLabel_1;
	private static JLabel lblNewLabel_2;
	private static JLabel lblNewLabel_3;


	private static final String driver = "com.mysql.cj.jdbc.Driver";
	private static final String user = "root";
	private static final String pass = "administrador";
	private static final String url = "jdbc:mysql://localhost:3306/valenbicibd";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConexionBDD frame = new ConexionBDD();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ConexionBDD() {
		setTitle("Conexión Base de Datos Valenbisi");
		dJSon = new DatosJSon(numEst);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 537, 425);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// Componentes de Texto y Etiquetas
		textAreaDatos = new JTextArea();
		textAreaDatos.setBounds(146, 74, 360, 168);
		contentPane.add(textAreaDatos);

		JScrollPane scrollPane = new JScrollPane(textAreaDatos); // Le pasas el textArea como parámetro
		scrollPane.setBounds(179, 64, 317, 190); // Se define el tamaño en la ventana

		// Solo barra vertical (opcional)
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// Añadimos el SCROLLPANE al panel principal
		contentPane.add(scrollPane);

		textFieldNEstaciones = new JTextField();
		textFieldNEstaciones.setBounds(410, 11, 86, 20);
		contentPane.add(textFieldNEstaciones);
		textFieldNEstaciones.setColumns(10);
		textFieldNEstaciones.setText("" + numEst);

		lblNewLabel = new JLabel("Introduce el número de estaciones a consultar:");
		lblNewLabel.setBounds(28, 14, 275, 14);
		contentPane.add(lblNewLabel);

		lblNewLabel_1 = new JLabel("Obtener Datos de Estaciones:");
		lblNewLabel_1.setBounds(180, 39, 254, 20);
		contentPane.add(lblNewLabel_1);

		lblNewLabel_2 = new JLabel("Estado Conexión:");
		lblNewLabel_2.setBounds(179, 265, 248, 14);
		contentPane.add(lblNewLabel_2);

		lblNewLabel_3 = new JLabel("Primero Obtener Datos de Estaciones y Conectar con BDD");
		lblNewLabel_3.setBounds(179, 299, 278, 14);
		contentPane.add(lblNewLabel_3);

		// BOTÓN 1: MOSTRAR DATOS EN TEXTAREA 
		JButton btnNewButtonDatos = new JButton("Datos"); 
		btnNewButtonDatos.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				try {
					// 1. Leemos el número que haya escrito el usuario en el cuadro de texto
					int numeroEstaciones = Integer.parseInt(textFieldNEstaciones.getText());

					// 2. Llamamos al método de tu clase DatosJSon para que baje los datos de internet
					dJSon.mostrarDatos(numeroEstaciones);

					// 3. Volcamos el texto resultante en el JTextArea de la pantalla
					textAreaDatos.setText(dJSon.getDatos());

				} catch (NumberFormatException ex) {
					textAreaDatos.setText("Por favor, introduce un número válido de estaciones.");
				}
			} 
		}); 
		btnNewButtonDatos.setBounds(31, 39, 111, 20); 
		contentPane.add(btnNewButtonDatos); 

		// BOTÓN 2: CONECTAR 
		JButton btnNewButtonConectar = new JButton("Conectar"); 
		btnNewButtonConectar.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				
				con = conector(); // <-- Llama al método de abajo y guarda la conexión
				
				if (con != null) {
					lblNewLabel_2.setText("Estado Conexión: ¡CONECTADO!");
				} else {
					lblNewLabel_2.setText("Estado Conexión: ERROR");
				}
			} 
		});
		btnNewButtonConectar.setBounds(28, 261, 111, 23); 
		contentPane.add(btnNewButtonConectar); 

		// BOTÓN 3: AÑADIR A BDD 
		JButton btnNewButtonAdd = new JButton("Añadir a BDD"); 
		btnNewButtonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Comprobamos si hay conexión activa antes de hacer nada
				if (con == null) {
					lblNewLabel_3.setText("Error: Primero debes conectar con la BDD.");
					return;
				}

				// Obtenemos el array de sentencias SQL generadas por la clase DatosJSon
				String[] sentenciasSQL = dJSon.getValues();

				if (sentenciasSQL == null || sentenciasSQL.length == 0 || sentenciasSQL[0].isEmpty()) {
					lblNewLabel_3.setText("Error: Primero debes pulsar el botón 'Datos'.");
					return;
				}

				try {
					int insertados = 0;
					// Recorremos el array e insertamos estación por estación
					for (String sql : sentenciasSQL) {
						if (sql != null && !sql.isEmpty()) {
							s.executeUpdate(sql);
							insertados++;
						}
					}
					lblNewLabel_3.setText("¡Éxito! Se han insertado " + insertados + " estaciones en la BDD.");
				} catch (SQLException ex) {
					lblNewLabel_3.setText("Error al insertar en la BDD: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});
		btnNewButtonAdd.setBounds(31, 295, 111, 23); 
		contentPane.add(btnNewButtonAdd); 

		// BOTÓN 4: CERRAR CONEXIÓN 
		JButton btnNewButtonCerrarConex = new JButton("Cerrar Conexión"); 
		btnNewButtonCerrarConex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Si la conexión existe, la cerramos de forma segura
					if (con != null && !con.isClosed()) {
						con.close();
						con = null; // Reseteamos la variable
						lblNewLabel_2.setText("Estado Conexión: Desconectado");
						lblNewLabel_3.setText("Conexión cerrada correctamente.");
					} else {
						lblNewLabel_3.setText("No hay ninguna conexión abierta para cerrar.");
					}
				} catch (SQLException ex) {
					lblNewLabel_3.setText("Error al cerrar conexión: " + ex.getMessage());
				}
			}
		});
		btnNewButtonCerrarConex.setBounds(179, 340, 124, 23); 
		contentPane.add(btnNewButtonCerrarConex); 
	}
	
	public Connection conector() { 
		con = null; 
		try { 
			Class.forName(driver); 
			con = DriverManager.getConnection(url, user, pass); 
			s = con.createStatement(); 
			return con; 
		} catch (ClassNotFoundException | SQLException e) { 
			e.printStackTrace();
			return null; 
		} 
	}
}