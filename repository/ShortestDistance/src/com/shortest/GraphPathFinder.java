package com.shortest;


import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class GraphPathFinder extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int WINDOW_SIZE = 800;
    private static final int POINT_RADIUS = 4;
    private static final int INFINITY = Integer.MAX_VALUE;
    
    private Point[] points;
    private int[][] adjacencyMatrix;
    private int n;
    private int[] shortestPath;
    private int shortestDistance;
    
    private JPanel drawingPanel;
    
    public GraphPathFinder() {
        setTitle("Поиск кратчайшего пути");
        setSize(WINDOW_SIZE, WINDOW_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        drawingPanel = new JPanel() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPoints(g);
                if (shortestPath != null) {
                    drawPath(g);
                }
            }
        };
        drawingPanel.setBackground(Color.WHITE);
        add(drawingPanel);
        
        // Read file and setup points
        String filePath = JOptionPane.showInputDialog("Введите путь к файлу с координатами:");
        if (filePath == null || filePath.trim().isEmpty()) {
            filePath = "pryam.txt"; // default file
        }
        
        try {
            points = readPointsFromFile(filePath);
            n = points.length;
            
            // Get start and end points from user
            String startInput = JOptionPane.showInputDialog("Всего точек: " + n + 
                    "\nВведите номер начальной точки (1-" + n + "):");
            String endInput = JOptionPane.showInputDialog("Введите номер конечной точки (1-" + n + "):");
            
            if (startInput == null || endInput == null) {
                System.exit(0);
            }
            
            int startNum = Integer.parseInt(startInput);
            int endNum = Integer.parseInt(endInput);
            
            if (startNum < 1 || startNum > n || endNum < 1 || endNum > n) {
                JOptionPane.showMessageDialog(this, "Некорректные номера точек");
                System.exit(1);
            }
            
            int startId = startNum - 1;
            int endId = endNum - 1;
            
            createAdjacencyMatrix(startId, endId);
            
            // Find shortest path
            DijkstraResult result = dijkstra(startId, endId);
            shortestDistance = result.distance;
            shortestPath = result.path;
            
            if (shortestDistance == INFINITY) {
                // Try direct connection if no path found
                adjacencyMatrix[startId][endId] = distance(points[startId], points[endId]);
                adjacencyMatrix[endId][startId] = adjacencyMatrix[startId][endId];
                
                result = dijkstra(startId, endId);
                shortestDistance = result.distance;
                shortestPath = result.path;
                
                if (shortestDistance == INFINITY) {
                    JOptionPane.showMessageDialog(this, "Нет возможных путей между точками");
                    System.exit(1);
                } else {
                    JOptionPane.showMessageDialog(this, "Использовано соединение напрямую, т.к. других путей нет");
                }
            }
            
            // Display results
            System.out.println("Кратчайшее расстояние между точками " + startNum + " и " + endNum + ": " + shortestDistance);
            System.out.println("Путь: ");
            for (int node : shortestPath) {
                System.out.print((node + 1) + " ");
            }
            System.out.println();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка чтения файла: " + e.getMessage());
            System.exit(1);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Некорректный ввод чисел");
            System.exit(1);
        }
    }
    
    private Point[] readPointsFromFile(String filePath) throws IOException {
        ArrayList<Point> pointList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        int index = 0;
        
        while ((line = reader.readLine()) != null) {
            String[] coords = line.trim().split("\\s+");
            if (coords.length >= 2) {
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                pointList.add(new Point(x, y, index + 1));
                index++;
            }
        }
        reader.close();
        
        return pointList.toArray(new Point[0]);
    }
    
    private void drawPoints(Graphics g) {
        if (points == null) return;
        
        for (Point p : points) {
            g.setColor(Color.GREEN);
            g.fillOval(p.x - POINT_RADIUS, p.y - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);
            g.setColor(Color.BLACK);
            g.drawString(Integer.toString(p.num), p.x + 10, p.y - 10);
        }
    }
    
    private void drawPath(Graphics g) {
        if (shortestPath == null || shortestPath.length < 2) return;
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));
        
        for (int i = 0; i < shortestPath.length - 1; i++) {
            Point p1 = points[shortestPath[i]];
            Point p2 = points[shortestPath[i + 1]];
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            
            // Draw step info
            g2d.setColor(Color.RED);
            g2d.drawString("Шаг " + (i + 1) + ": " + p1.num, p1.x + 10, p1.y - 10);
        }
        
        // Draw last point info
        Point last = points[shortestPath[shortestPath.length - 1]];
        g2d.drawString("Шаг " + shortestPath.length + ": " + last.num, last.x + 10, last.y - 10);
        
        // Draw start/end labels
        g2d.setColor(Color.GREEN);
        Point start = points[shortestPath[0]];
        Point end = points[shortestPath[shortestPath.length - 1]];
        g2d.drawString("Start", start.x, start.y - 25);
        g2d.drawString("End", end.x, end.y - 25);
    }
    
    private int distance(Point p1, Point p2) {
        return (int) Math.round(Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2)));
    }
    
    private void createAdjacencyMatrix(int startId, int endId) {
        adjacencyMatrix = new int[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if ((i == startId && j == endId) || (i == endId && j == startId)) {
                    adjacencyMatrix[i][j] = 0;
                } else if (i == j) {
                    adjacencyMatrix[i][j] = 0;
                } else {
                    adjacencyMatrix[i][j] = distance(points[i], points[j]);
                }
            }
        }
    }
    
    private DijkstraResult dijkstra(int start, int finish) {
        int[] dist = new int[n];
        boolean[] visited = new boolean[n];
        int[] previous = new int[n];
        
        Arrays.fill(dist, INFINITY);
        Arrays.fill(previous, -1);
        dist[start] = 0;
        
        for (int i = 0; i < n; i++) {
            int u = -1;
            int minDist = INFINITY;
            
            for (int v = 0; v < n; v++) {
                if (!visited[v] && dist[v] < minDist) {
                    minDist = dist[v];
                    u = v;
                }
            }
            
            if (u == -1) break;
            if (u == finish) break;
            
            visited[u] = true;
            
            for (int v = 0; v < n; v++) {
                if (!visited[v] && adjacencyMatrix[u][v] > 0) {
                    int alt = dist[u] + adjacencyMatrix[u][v];
                    if (alt < dist[v]) {
                        dist[v] = alt;
                        previous[v] = u;
                    }
                }
            }
        }
        
        if (dist[finish] == INFINITY) {
            return new DijkstraResult(INFINITY, new int[0]);
        }
        
        // Reconstruct path
        ArrayList<Integer> pathList = new ArrayList<>();
        int u = finish;
        while (previous[u] != -1) {
            pathList.add(u);
            u = previous[u];
        }
        pathList.add(start);
        
        Collections.reverse(pathList);
        
        int[] path = new int[pathList.size()];
        for (int i = 0; i < path.length; i++) {
            path[i] = pathList.get(i);
        }
        
        return new DijkstraResult(dist[finish], path);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GraphPathFinder finder = new GraphPathFinder();
            finder.setVisible(true);
        });
    }
    
    private static class Point {
        int x, y, num;
        
        public Point(int x, int y, int num) {
            this.x = x;
            this.y = y;
            this.num = num;
        }
    }
    
    private static class DijkstraResult {
        int distance;
        int[] path;
        
        public DijkstraResult(int distance, int[] path) {
            this.distance = distance;
            this.path = path;
        }
    }
}