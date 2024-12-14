package org.example;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GraphVisualizer {

    private static final Logger logger = LogManager.getLogger(GraphVisualizer.class);

    public static void displayGraph(int numNodes, int numEdges, Stage stage, Label cyclesLabel) {
        logger.info("Начинаем визуализацию графика с {} вершин и {} рёбер.", numNodes, numEdges);

        Pane pane = new Pane();
        Map<Integer, List<Integer>> graph = generateRandomGraph(numNodes, numEdges);

        Map<Integer, Circle> nodeMap = IntStream.range(0, numNodes)
                .boxed()
                .collect(Collectors.toMap(
                        Function.identity(),
                        i -> {
                            double angle = Math.toRadians((double) (i * 360) / numNodes);
                            double x = 300 + 150 * Math.cos(angle);
                            double y = 300 + 150 * Math.sin(angle);

                            Circle circle = new Circle(x, y, 20, Color.LIGHTBLUE);
                            Text text = new Text(x - 7, y + 5, String.valueOf(i));

                            pane.getChildren().addAll(circle, text);
                            return circle;
                        }
                ));

        displayEdges(pane, graph, nodeMap);
        int loopCount = countLoops(graph);

        // Логирование количества петель
        logger.info("Количество петель в графе: {}", loopCount);

        cyclesLabel.setText("Количество петель: " + loopCount);

        Scene scene = new Scene(pane, 600, 600);
        stage.setScene(scene);

        logger.info("Визуализация графа завершена.");
    }

    private static void displayEdges(Pane pane, Map<Integer, List<Integer>> graph, Map<Integer, Circle> nodeMap) {
        logger.debug("Отображение ребер на графе: {}", graph);

        Map<Integer, Integer> loopCounts = new HashMap<>();

        graph.forEach((node, neighbors) -> {
            double x1 = nodeMap.get(node).getCenterX();
            double y1 = nodeMap.get(node).getCenterY();

            neighbors.forEach(neighbor -> {
                double x2 = nodeMap.get(neighbor).getCenterX();
                double y2 = nodeMap.get(neighbor).getCenterY();

                if (node.equals(neighbor)) {
                    // Обработка петли
                    if (loopCounts.getOrDefault(node, 0) == 0) {
                        drawLoop(pane, x1, y1, loopCounts, node);
                        loopCounts.put(node, 1);
                    }
                } else {
                    // Обработка обычного ребра
                    Line line = new Line(x1, y1, x2, y2);
                    line.setStroke(Color.BLACK);
                    pane.getChildren().add(line);
                    addArrow(pane, line);
                }
            });
        });

        logger.debug("Рёбра отобразились успешно.");
    }

    private static void drawLoop(Pane pane, double x1, double y1, Map<Integer, Integer> loopCounts, int node) {
        logger.debug("Рисование петли для вершины: {}", node);

        int loopIndex = loopCounts.getOrDefault(node, 0);
        double offset = loopIndex * 25;

        double loopSize = 40;

        CubicCurve loop = new CubicCurve(
                x1, y1,
                x1 + loopSize + offset, y1 - loopSize * 1.5,
                x1 - loopSize - offset, y1 - loopSize * 1.5,
                x1, y1
        );

        loop.setStroke(Color.DARKBLUE);
        loop.setStrokeWidth(2);
        loop.setFill(Color.TRANSPARENT);
        pane.getChildren().add(loop);
        loopCounts.put(node, loopIndex + 1);

        logger.debug("Петля успешно отрисован для узла: {}", node);
    }

    private static void addArrow(Pane pane, Line line) {
        logger.debug("Добавление стрелки к линии: {}", line);

        double arrowLength = 8;

        double x1 = line.getStartX();
        double y1 = line.getStartY();
        double x2 = line.getEndX();
        double y2 = line.getEndY();

        double angle = Math.atan2(y2 - y1, x2 - x1);

        double xArrow1 = x2 - arrowLength * Math.cos(angle - Math.toRadians(30));
        double yArrow1 = y2 - arrowLength * Math.sin(angle - Math.toRadians(30));

        double xArrow2 = x2 - arrowLength * Math.cos(angle + Math.toRadians(30));
        double yArrow2 = y2 - arrowLength * Math.sin(angle + Math.toRadians(30));

        Polygon arrow = new Polygon(
                x2, y2,
                xArrow1, yArrow1,
                xArrow2, yArrow2
        );
        arrow.setFill(Color.BLACK);

        pane.getChildren().add(arrow);

        logger.debug("Стрелка успешно добавлена к линии: {}", line);
    }

    private static Map<Integer, List<Integer>> generateRandomGraph(int numNodes, int numEdges) {
        logger.info("Генерация случайного графа с {} вершинами и {} рёбрами.", numNodes, numEdges);

        // Готовим граф: каждая вершина изначально пустая
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (int i = 0; i < numNodes; i++) {
            graph.put(i, new ArrayList<>());
        }

        // Специальный случай: 2 вершины и 2 рёбра (одна петля и одно ребро)
        if (numNodes == 2 && numEdges == 2) {
            graph.get(0).add(0);// Петля на вершине 0
            graph.get(0).add(1); // Ребро между вершинами 0 и 1
            return graph;
        }

        if (numNodes == 2 && numEdges == 3) {
            graph.get(0).add(0);// Петля на вершине 0
            graph.get(1).add(1);
            graph.get(0).add(1); // Ребро между вершинами 0 и 1
            return graph;
        }

        Random random = new Random();
        int edgesAdded = 0;
        int loopsCount = 0;

        // Создаём связный граф (дерево), чтобы каждая вершина была соединена хотя бы с одной другой
        for (int i = 1; i < numNodes; i++) {
            int parent = random.nextInt(i);
            graph.get(i).add(parent);
            graph.get(parent).add(i);
            edgesAdded++;
        }

        // Добавляем оставшиеся рёбра случайным образом
        while (edgesAdded < numEdges) {
            int from = random.nextInt(numNodes);
            int to = random.nextInt(numNodes);

            // Проверяем петлю
            if (from == to) {
                if (!graph.get(from).contains(to)) {
                    graph.get(from).add(to); // Петля
                    edgesAdded++;
                    loopsCount++; // Увеличиваем счётчик петель
                }
            } else {
                // Проверяем связь между различными узлами
                if (!graph.get(from).contains(to) && !graph.get(to).contains(from)) {
                    graph.get(from).add(to);
                    graph.get(to).add(from);
                    edgesAdded++;
                }
            }
        }

        logger.info("Количество сгенерированных петель: {}", loopsCount);
        return graph;
    }

    private static int countLoops(Map<Integer, List<Integer>> graph) {
        logger.debug("Подсчет петель в графе: {}", graph);

        int loopCount = graph.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .filter(neighbor -> entry.getKey().equals(neighbor))
                )
                .mapToInt(loop -> 1)
                .sum();

        logger.debug("Количество петель: {}", loopCount);
        return loopCount;
    }
}
