package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GraphApp extends Application {

    private static final Logger logger = LogManager.getLogger(GraphApp.class);

    private Stage graphStage; // Поле для хранения окна графа

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Графический калькулятор петель");

        // Панель для размещения элементов интерфейса
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        // Ввод количества узлов
        Label nodesLabel = new Label("Количество вершин:");
        GridPane.setConstraints(nodesLabel, 0, 0);
        TextField nodesField = new TextField("5");
        GridPane.setConstraints(nodesField, 1, 0);

        // Ввод количества рёбер
        Label edgesLabel = new Label("Количество рёбер:");
        GridPane.setConstraints(edgesLabel, 0, 1);
        TextField edgesField = new TextField("7");
        GridPane.setConstraints(edgesField, 1, 1);

        // Кнопка для генерации графа
        Button generateButton = new Button("Сгенерировать граф");
        GridPane.setConstraints(generateButton, 1, 2);

        // Метка для отображения времени
        Label timeLabel = new Label("Время построения графа: ");
        GridPane.setConstraints(timeLabel, 0, 3);

        // Метка для отображения количества циклов
        Label cyclesLabel = new Label("Количество петель: ");
        GridPane.setConstraints(cyclesLabel, 0, 4);

        // Добавление всех элементов в панель
        grid.getChildren().addAll(nodesLabel, nodesField, edgesLabel, edgesField, generateButton, timeLabel, cyclesLabel);

        // Создание сцены для главного окна
        Scene mainScene = new Scene(grid, 400, 300);
        primaryStage.setScene(mainScene);

        // Обработчик события нажатия на кнопку "Сгенерировать граф"
        generateButton.setOnAction(e -> {
            try {
                int numNodes = Integer.parseInt(nodesField.getText());
                int numEdges = Integer.parseInt(edgesField.getText());

                logger.info("Пользователь ввёл количество вершин: {} и рёбер: {}", numNodes, numEdges);

                if (numNodes <= 0 || numEdges < 0) {
                    showAlert("Введите действительное число");
                    logger.warn("Некорректный ввод: количество вершин или рёбер меньше или равно нулю");
                    return;
                }

                // Проверяем, достаточно ли рёбер для соединения всех вершин
                if (numEdges < numNodes - 1) {
                    logger.error("Недостаточно рёбер для соединения всех вершин. Минимум нужно {} рёбер.", numNodes - 1);
                    showAlert("Недостаточно рёбер для соединения всех вершин.");
                    return;
                }

                // Проверка на случай, когда пользователь вводит 1 вершину и 1 ребро
                if (numNodes == 1 && numEdges == 1) {
                    logger.info("Пользователь ввёл 1 вершину и 1 ребро, рисуем петлю");
                    updateGraphWindow(numNodes, numEdges, cyclesLabel);
                    return;
                }

                // Проверка на случай, когда пользователь вводит 2 вершины и 2 ребра
                if (numNodes == 2 && numEdges == 2) {
                    updateGraphWindow(numNodes, numEdges, cyclesLabel); // Передаем 2 ребра для отображения
                    return;
                }

                if (numNodes == 2 && numEdges == 3) {
                    updateGraphWindow(numNodes, numEdges, cyclesLabel); // Передаем 2 ребра для отображения
                    return;
                }

                if (numEdges > numNodes * (numNodes - 1) / 2) {
                    showAlert("Слишком большое количество рёбер для заданных вершин");
                    logger.warn("Некорректный ввод: слишком большое количество рёбер для вершин");
                    return;
                }

                // Засекаем время начала генерации
                long startTime = System.nanoTime();
                logger.info("Начало генерации графа");

                // Генерация списка узлов с использованием Stream API
                List<String> nodes = IntStream.range(0, numNodes)
                        .mapToObj(i -> "Node" + i)
                        .collect(Collectors.toList());

                // Логирование списка узлов
                logger.debug("Сгенерированные вершины: {}", nodes);

                // Обновляем или создаем окно графа
                updateGraphWindow(numNodes, numEdges, cyclesLabel);

                // Засекаем время после генерации
                long endTime = System.nanoTime();
                logger.info("Граф успешно сгенерирован");

                // Вычисление времени в миллисекундах
                double elapsedTime = (endTime - startTime) / 1000000.0;

                // Обновление метки с временем
                timeLabel.setText("Время построения графа: " + elapsedTime + " ms");
                logger.info("Время построения графа: {} ms", elapsedTime);

            } catch (NumberFormatException ex) {
                showAlert("Введите действительное число");
                logger.error("Ошибка ввода: неверный формат числа", ex);
            }
        });

        primaryStage.show();
        logger.info("Приложение запущено");
    }

    private void updateGraphWindow(int numNodes, int numEdges, Label cyclesLabel) {
        // Если окно графа уже существует, обновляем его сцену
        if (graphStage == null) {
            graphStage = new Stage();
            graphStage.setTitle("Граф");
            logger.info("Создано новое окно для отображения графа");
        }

        // Вызываем метод для обновления содержимого окна
        GraphVisualizer.displayGraph(numNodes, numEdges, graphStage, cyclesLabel);
        logger.info("Обновлено содержимое окна графа");

        if (!graphStage.isShowing()) {
            graphStage.show();
            logger.info("Окно графа отображено");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        logger.warn("Отображено предупреждение: {}", message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
