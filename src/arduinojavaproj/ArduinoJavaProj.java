package arduinojavaproj;

import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.LinkedList;

public class ArduinoJavaProj {

    static private SerialPort chosenPort;
    static private JList<String> jList;
    static private DefaultListModel<String> model;


    public static void main(String[] args) {

        // create and configure the window
        JFrame window = new JFrame();
        window.setTitle("Arduino LCD Clock");
        window.setSize(600, 300);
        window.setLayout(new BorderLayout());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // create a drop-down box and connect button, then place them at the top of the window
        JComboBox<String> portList = new JComboBox<String>();
        JButton connectButton = new JButton("Connect");
        JButton getDataButton = new JButton("GET Data");

        model = new DefaultListModel<String>();
        jList = new JList<String>(model);

        JScrollPane area = new JScrollPane(jList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        area.setPreferredSize(new Dimension(500, 200));

        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();

        topPanel.add(portList);
        topPanel.add(connectButton);
        topPanel.add(getDataButton);

        bottomPanel.add(area);
        window.add(topPanel, BorderLayout.NORTH);
        window.add(bottomPanel, BorderLayout.SOUTH);

        // populate the drop-down box
        SerialPort[] portNames = SerialPort.getCommPorts();
        for (int i = 0; i < portNames.length; i++)
            portList.addItem(portNames[i].getSystemPortName());


        // configure the connect button and use another thread to listen for data
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (connectButton.getText().equals("Connect")) {
                    // attempt to connect to the serial port
                    chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
                    chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
                    if (chosenPort.openPort()) {
                        connectButton.setText("Disconnect");
                        portList.setEnabled(false);
                    }
                } else {
                    // disconnect from the serial port
                    chosenPort.closePort();
                    portList.setEnabled(true);
                    connectButton.setText("Connect");
                }
            }
        });

        getDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getHTTPResponse();
            }
        });

        ListSelectionModel selectionModel = jList.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                
                if (!lsm.isSelectionEmpty()) {
                    if (!e.getValueIsAdjusting()) {
                        if (chosenPort.isOpen()) {
                            //create a new thread that will send data to the arduino
                            Thread thread = new Thread() {
                                @Override
                                public void run() {


                                    PrintWriter output = new PrintWriter(chosenPort.getOutputStream());

                                    //enter inifinte loop that send text to the arduino
                                    String temp = model.get(e.getFirstIndex()).replaceAll("[^a-zA-Z0-9]+!@#$%^&*()_+=-?><,. /", "");
                                    output.print(rewriteString(temp));
                                    output.flush();
                                    
                                    lsm.clearSelection();

                                }
                            };
                            thread.start();
                        }
                    }
                }
            }
        });

        // show the window
        window.setVisible(true);

    }


    public static void getHTTPResponse() {

        try {
            URL url = new URL("https://graph.facebook.com/v2.11/pensiuneajoiedevivre/posts?access_token=EAAaZCt5Oheh8BACe0ZB4VJ3alhgZAMZAkKWTnVkjZAnP4tDqHXroQulIXHdUqnb0mCwkIRiw3RoCAY4wyBhJDUxCcJAjxSjnf7bNQlp6dGI7sdUkbDtplGnlugu0Jgw6cZCJjeKZCFbroczXjYJN3OvqFU3YyQgRDOvcMgJPql9agZDZD");
            BufferedReader reader = null;

            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            //create the connection and open it
            reader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
            // start reading the response given by the HTTP response

            StringBuilder jsonString = new StringBuilder();

            // using string builder as it is more efficient for append operations
            String line;
            // append the string response to our jsonString variable
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            // dont forget to close the reader
            reader.close();
            // close the http connection
            httpsURLConnection.disconnect();
            // start parsing
            parseJSON(jsonString.toString());
        } catch (IOException e1) {
            System.out.println("Malformed URL or issues with the reader.");
            e1.printStackTrace();
        }
    }

    private static void parseJSON(String jsonResponse)  {
        try {
            
        JSONObject myresponse = new JSONObject(jsonResponse);
        JSONArray posts = myresponse.getJSONArray("data");

        if (posts.length() > 0) {
            model.clear();
            for (int i = 0; i < posts.length(); i++) {
                try {
                    String rez = String.valueOf(((JSONObject) posts.get(i)).get("message"));
                    if (rez != null && rez.length() > 0) {
                        model.addElement(rez.trim());
                    }
                } catch (Exception e) {
                }
            }
        }
        } catch(Exception e) {
            
        }
    }

    public static String rewriteString(String res) {

        LinkedList<Integer> linkedList = new LinkedList<>();
        int index = res.indexOf(" ");
        while (index >= 0) {
            linkedList.add(index);
            index = res.indexOf(" ", index + 1);
        }


        LinkedList<String> strings = new LinkedList<>();
        String rex = res;

        while (linkedList.size() > 1) {

            int indexP = linkedList.size() - 1;
            while (linkedList.get(indexP) > 15) {
                indexP--;
            }

            Integer tmpIndex = linkedList.get(indexP);
            String tmpString = rex.substring(0, tmpIndex);
            strings.add(tmpString);
            rex = rex.substring(tmpIndex + 1);
            linkedList.clear();


            index = rex.indexOf(" ");
            while (index >= 0) {
                linkedList.add(index);
                index = rex.indexOf(" ", index + 1);
            }
        }

        strings.add(rex);
        String output = "";
        for (String str : strings) {
            output += str.trim() + "|";
        }

        return output;

    }

}


