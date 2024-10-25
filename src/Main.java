import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Исключение, выбрасываемое при попытке выполнить действие без выбора строки.
 */
class InvalidSelectionException extends Exception {
    public InvalidSelectionException(String message) {
        super(message);
    }
}

public class Main {
    private JFrame mainFrame;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JButton addDogButton, editDogButton, deleteDogButton, loadDogButton, saveDogButton;
    private JTextField searchField;
    private JComboBox<String> searchCriteriaComboBox;
    private boolean unsavedChanges = false;

    /**
     * Метод для построения и визуализации экранной формы.
     */
    public void show() {
        mainFrame = new JFrame("Dog Show Administration");
        mainFrame.setSize(800, 400);
        mainFrame.setLocation(100, 100);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Создание кнопок
        addDogButton = new JButton("Добавить");
        editDogButton = new JButton("Изменить");
        deleteDogButton = new JButton("Удалить");
        loadDogButton = new JButton("Загрузить");
        saveDogButton = new JButton("Сохранить");

        // Панель инструментов с кнопками
        JToolBar toolBar = new JToolBar("Панель инструментов");
        toolBar.add(addDogButton);
        toolBar.add(editDogButton);
        toolBar.add(deleteDogButton);
        toolBar.add(loadDogButton);
        toolBar.add(saveDogButton);

        mainPanel.add(toolBar, BorderLayout.NORTH);

        // Данные для таблицы
        String[] columns = {"Кличка", "Порода", "Владелец", "Судья", "Награды"};
        tableModel = new DefaultTableModel(columns, 0);
        dataTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dataTable);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Элементы для поиска
        searchField = new JTextField(15);
        searchCriteriaComboBox = new JComboBox<>(new String[]{"По породе", "По владельцу", "По судье"});
        JButton searchButton = new JButton("Поиск");

        JPanel searchPanel = new JPanel();
        searchPanel.add(searchCriteriaComboBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        mainPanel.add(searchPanel, BorderLayout.SOUTH);
        mainFrame.add(mainPanel);

        // Логика для кнопки "Добавить"
        addDogButton.addActionListener(e -> {
            tableModel.addRow(new Object[]{"Новая собака", "Неизвестная порода", "Новый владелец", "Новый судья", "Нет наград"});
            unsavedChanges = true;
            JOptionPane.showMessageDialog(mainFrame, "Добавлена новая собака");
        });

        // Логика для кнопки "Изменить"
        editDogButton.addActionListener(e -> {
            try {
                validateSelectionForEdit(dataTable);
                JOptionPane.showMessageDialog(mainFrame, "Информация изменена");
            } catch (InvalidSelectionException ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
            }
        });

        // Логика для кнопки "Удалить"
        deleteDogButton.addActionListener(e -> {
            try {
                validateSelection(dataTable);
                int selectedRow = dataTable.getSelectedRow();
                tableModel.removeRow(selectedRow);
                unsavedChanges = true;
                JOptionPane.showMessageDialog(mainFrame, "Запись удалена");
            } catch (InvalidSelectionException ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage());
            }
        });

        // Реализация кнопки "Загрузить"
        loadDogButton.addActionListener(e -> loadDataFromFile());

        // Реализация кнопки "Сохранить"
        saveDogButton.addActionListener(e -> saveDataToFile());

        // Обработка закрытия окна с проверкой на несохраненные изменения
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (unsavedChanges) {
                    int response = JOptionPane.showConfirmDialog(mainFrame, "Есть несохраненные изменения. Хотите сохранить перед выходом?", "Несохраненные изменения", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        saveDataToFile();
                        mainFrame.dispose();
                    } else if (response == JOptionPane.NO_OPTION) {
                        mainFrame.dispose();
                    }
                } else {
                    mainFrame.dispose();
                }
            }
        });

        mainFrame.setVisible(true);
    }

    /**
     * Метод для загрузки данных из файла в таблицу.
     */
    private void loadDataFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(mainFrame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                tableModel.setRowCount(0); // Очистка таблицы перед загрузкой данных
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] rowData = line.split(";"); // Разделение данных запятыми
                    tableModel.addRow(rowData);
                }
                unsavedChanges = true;
                JOptionPane.showMessageDialog(mainFrame, "Данные успешно загружены");

            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных");
            }
        }
    }

    /**
     * Метод для сохранения данных из таблицы в файл.
     */
    private void saveDataToFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(mainFrame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        writer.write((String) tableModel.getValueAt(i, j));
                        if (j < tableModel.getColumnCount() - 1) {
                            writer.write(";");
                        }
                    }
                    writer.newLine();
                }
                unsavedChanges = false;
                JOptionPane.showMessageDialog(mainFrame, "Данные успешно сохранены");

            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainFrame, "Ошибка при сохранении данных");
            }
        }
    }

    /**
     * Метод проверки, выбрана ли строка в таблице для удаления.
     * @throws InvalidSelectionException если строка не выбрана
     */
    private void validateSelection(JTable table) throws InvalidSelectionException {
        if (table.getSelectedRow() == -1) {
            throw new InvalidSelectionException("Не выбрана строка для удаления.");
        }
    }

    /**
     * Метод проверки, выбрана ли строка в таблице для изменения.
     * @throws InvalidSelectionException если строка не выбрана
     */
    private void validateSelectionForEdit(JTable table) throws InvalidSelectionException {
        if (table.getSelectedRow() == -1) {
            throw new InvalidSelectionException("Не выбрана строка для изменения.");
        }
    }

    public static void main(String[] args) {
        new Main().show();
    }
}
