import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

public class Main extends JFrame {
    private JTextField txtTemperature, txtSale, txtPredict;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private double slope = 0, intercept = 0;
    private boolean modelTrained = false;

    public Main() {
        setTitle("Sales Prediction System");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel - Inputs and Buttons
        JPanel topPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topPanel.add(new JLabel("Temperature (1-50):"));
        txtTemperature = new JTextField();
        topPanel.add(txtTemperature);

        topPanel.add(new JLabel("Sales (1-500):"));
        txtSale = new JTextField();
        topPanel.add(txtSale);

        JButton btnAdd = new JButton("Add Data Point");
        JButton btnLoad = new JButton("Load Data");
        topPanel.add(btnAdd);
        topPanel.add(btnLoad);

        JButton btnDelete = new JButton("Delete All");
        JButton btnTrain = new JButton("Train Model");
        topPanel.add(btnDelete);
        topPanel.add(btnTrain);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel - Table
        tableModel = new DefaultTableModel(new String[]{"ID", "Temperature", "Sale"}, 0);
        dataTable = new JTable(tableModel);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        // Bottom Panel - Prediction
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(new JLabel("Predict Sales for Temp:"));
        txtPredict = new JTextField(5);
        bottomPanel.add(txtPredict);
        JButton btnPredict = new JButton("Predict Sale");
        bottomPanel.add(btnPredict);
        add(bottomPanel, BorderLayout.SOUTH);

        // Close Event - Developer Info
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                JOptionPane.showMessageDialog(null, "Developer: Zainab Batool\nStudent ID: BC220206416");
                System.exit(0);
            }
        });

        // Add Button Action
        btnAdd.addActionListener(e -> {
            try {
                int temp = Integer.parseInt(txtTemperature.getText());
                int sale = Integer.parseInt(txtSale.getText());
                if (temp < 1 || temp > 50 || sale < 1 || sale > 500) {
                    throw new NumberFormatException();
                }

                // Check if already exists
                List<DataPoint> allData = DbHelper.getAllData();
                boolean exists = allData.stream().anyMatch(d -> d.getTemperature() == temp && d.getSale() == sale);
                if (exists) {
                    JOptionPane.showMessageDialog(this, "Data point already exists!", "Duplicate", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                DbHelper.insertData(temp, sale);
                JOptionPane.showMessageDialog(this, "Data added successfully.");
                txtTemperature.setText("");
                txtSale.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input! Please enter values in correct range.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Load Button
        btnLoad.addActionListener(e -> {
            tableModel.setRowCount(0);
            try {
                List<DataPoint> list = DbHelper.getAllData();
                for (DataPoint dp : list) {
                    tableModel.addRow(new Object[]{dp.getId(), dp.getTemperature(), dp.getSale()});
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load data: " + ex.getMessage());
            }
        });

        // Delete Button
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete all records?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    DbHelper.deleteAll();
                    tableModel.setRowCount(0);
                    JOptionPane.showMessageDialog(this, "All records deleted.");
                    modelTrained = false;
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting records: " + ex.getMessage());
                }
            }
        });

        // Train Button
        btnTrain.addActionListener(e -> {
            try {
                List<DataPoint> data = DbHelper.getAllData();
                int n = data.size();
                if (n < 2) {
                    JOptionPane.showMessageDialog(this, "At least 2 data points required to train.");
                    return;
                }

                double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
                for (DataPoint d : data) {
                    int x = d.getTemperature();
                    int y = d.getSale();
                    sumX += x;
                    sumY += y;
                    sumXY += x * y;
                    sumX2 += x * x;
                }

                slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
                intercept = (sumY - slope * sumX) / n;
                modelTrained = true;
                JOptionPane.showMessageDialog(this, "Model trained.\nSlope (m): " + slope + "\nIntercept (b): " + intercept);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Training error: " + ex.getMessage());
            }
        });

        // Predict Button
        btnPredict.addActionListener(e -> {
            if (!modelTrained) {
                JOptionPane.showMessageDialog(this, "Please train the model first.");
                return;
            }

            try {
                int temp = Integer.parseInt(txtPredict.getText());
                if (temp < 1 || temp > 50) throw new NumberFormatException();

                double predictedSale = slope * temp + intercept;
                int rounded = (int) Math.round(predictedSale);
                int option = JOptionPane.showConfirmDialog(this,
                        "Predicted Sale: " + rounded +
                                "\nSlope: " + slope +
                                "\nIntercept: " + intercept +
                                "\n\nDo you want to save this prediction?",
                        "Prediction", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    DbHelper.insertData(temp, rounded);
                    JOptionPane.showMessageDialog(this, "Prediction saved.");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input! Must be 1–50.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error saving prediction: " + ex.getMessage());
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
