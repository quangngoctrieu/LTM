package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.StringUtils;

import model.Client;
import model.Message;
import model.Status;

import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class UserFrame extends JFrame {

	public int i = 0;
	public JPanel panel_main;
	public JPanel panel_thanhphan;
	public String ip;
	public int port;
	private JScrollPane scroll;
	private JButton btn_Gui;
	private JButton btn_KetNoi;
	
	private JTextArea AreaChat;
	private JTextField txt_TinNhan;
	private JTextField txt_Name;
	private Client client;
	private JButton btn_Thoat;
	private volatile boolean running = true;
	Thread t;

	public UserFrame() throws UnknownHostException, IOException {
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (client != null) {
					try {
						running = false;
						Message send = new Message(null, null, Status.EXIT);
						System.out.println(send);
						client.sendMessage(send);
						client.closeAll();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		setResizable(false);
		setSize(688, 600);

		setLocationRelativeTo(null);
		setTitle("Chat App");
		panel_main = new JPanel();
		panel_main.setBackground(Color.WHITE);
		panel_main.setLayout(new BorderLayout(0, 0));
		setContentPane(panel_main);

		ip = "localhost";
		port = 1234;

		panel_thanhphan = new JPanel();
		panel_thanhphan.setBorder(new LineBorder(Color.WHITE, 0, true));
		panel_thanhphan.setPreferredSize(new Dimension(180, 650));
		panel_thanhphan.setBackground(Color.LIGHT_GRAY);
		panel_thanhphan.setLayout(null);
		panel_main.add(panel_thanhphan, BorderLayout.CENTER);

		AreaChat = new JTextArea();
		AreaChat.setEditable(false);

		scroll = new JScrollPane(AreaChat);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(29, 70, 619, 348);
		panel_thanhphan.add(scroll);

		txt_TinNhan = new JTextField();
		txt_TinNhan.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (txt_TinNhan.getText().length() != 0) {
					btn_Gui.setEnabled(true);
				} else {
					btn_Gui.setEnabled(false);
				}
			}
		});
		txt_TinNhan.setBounds(29, 474, 507, 63);
		panel_thanhphan.add(txt_TinNhan);
		txt_TinNhan.setColumns(10);

		btn_Gui = new JButton("Gửi");
		btn_Gui.setEnabled(false);
		btn_Gui.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if(client == null) {
						JOptionPane.showMessageDialog(null,
								"Chưa kết nối mà send !",
								"Thông báo", JOptionPane.ERROR_MESSAGE);
					}else {
						if (client.isMatched()) {
							if(!txt_TinNhan.getText().isEmpty()) {
								Message send = new Message(client.getName(), txt_TinNhan.getText(), Status.CHAT);
								client.sendMessage(send);
								AreaChat.append(client.getName() + " : " + txt_TinNhan.getText() + "\n");
								txt_TinNhan.setText("");
								btn_Gui.setEnabled(false);
							} else {
								JOptionPane.showMessageDialog(null,
										"Tin nhắn không được để trống !",
										"Thông báo", JOptionPane.ERROR_MESSAGE);
							}
							
						} else {
							System.out.println("Chưa ghép đôi mà đòi send");
						}
					}
					

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btn_Gui.setBounds(563, 474, 85, 63);
		panel_thanhphan.add(btn_Gui);

		txt_Name = new JTextField();
		txt_Name.setBounds(29, 21, 248, 28);
		panel_thanhphan.add(txt_Name);
		txt_Name.setColumns(10);

		btn_KetNoi = new JButton("Kết nối");
		btn_KetNoi.setBackground(Color.WHITE);
		btn_KetNoi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (txt_Name.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"Không được để trống tên !",
							"Thông báo", JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						if (client == null) {
							client = new Client(new Socket(ip, port), txt_Name.getText());
							Message welcome = new Message(txt_Name.getText(), null, Status.CONNECT);
							client.sendMessage(welcome);
						} else {
							Message welcome = new Message(txt_Name.getText(), null, Status.CONNECT);
							client.sendMessage(welcome);
						}
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (i == 0) {
					t = new Thread(new Runnable() {
						@Override
						public void run() {
							while (running) {
								// TODO Auto-generated method stub
								try {
									Message receivedMessage = client.receiveMessage();
									System.out.println(receivedMessage);
									switch (receivedMessage.getStatus()) {
									case MATCH:
										int action = JOptionPane.showConfirmDialog(null,
												"Bạn có muốn ghép đôi với " + receivedMessage.getName() + "?",
												"Ghép đôi thành công", JOptionPane.YES_NO_OPTION);
										if (action == JOptionPane.OK_OPTION) {
											Message accept = new Message(client.getName(), null, Status.OK);
											client.sendMessage(accept);
											client.setMatched(true);
											btn_Thoat.setEnabled(true);
											AreaChat.setText("");
										} else {
											Message refuse = new Message(client.getName(), null, Status.REFUSE);
											client.sendMessage(refuse);
											btn_Thoat.setEnabled(false);
										}
										break;
									case CHAT:
										AreaChat.append(
												receivedMessage.getName() + " : " + receivedMessage.getData() + "\n");
										break;
									case EXIST:
										JOptionPane.showMessageDialog(null, "Tên trùng với người khác !", "Thông báo",
												JOptionPane.ERROR_MESSAGE);
										break;
									case UNMATCH:
										JOptionPane.showMessageDialog(null,
												"Người kia đã từ chối ghép đôi, bạn sẽ quay lại hàng chờ !",
												"Thông báo", JOptionPane.ERROR_MESSAGE);
										client.setMatched(false);
										btn_Thoat.setEnabled(false);
										break;
									case EXIT:
										JOptionPane.showMessageDialog(null,
												"Người kia đã thoát khỏi phòng chat, bạn sẽ quay lại hàng chờ !",
												"Thông báo", JOptionPane.ERROR_MESSAGE);
										client.setMatched(false);
										btn_Thoat.setEnabled(false);
										break;
									case CONNECTED:
										btn_KetNoi.setEnabled(false);
										txt_Name.setEditable(false);
										break;
									default:

									}

								} catch (IOException | ClassNotFoundException e) {
									System.out.println();
								}
							}
						}
					});
					t.start();
					i++;
				}
				}
			}
		});
		btn_KetNoi.setBounds(305, 20, 85, 30);
		panel_thanhphan.add(btn_KetNoi);

		
		
		
		btn_Thoat = new JButton("Hủy kết nối");
		btn_Thoat.setForeground(Color.BLACK);
		btn_Thoat.setFont(new Font("Arial", Font.BOLD, 10));
		btn_Thoat.setBackground(Color.WHITE);
		btn_Thoat.setEnabled(false);
		btn_Thoat.setBounds(563, 19, 85, 30);
		btn_Thoat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					try {
						Message end = new Message(null, null, Status.DISCONNECT);
						client.sendMessage(end);
						client.setMatched(false);
						btn_Thoat.setEnabled(false);
						AreaChat.setText("");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
		});
		panel_thanhphan.add(btn_Thoat);
	}
}
