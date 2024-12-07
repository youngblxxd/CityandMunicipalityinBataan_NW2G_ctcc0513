import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class CityAndMunicipalitiesRouteInBataan extends JFrame {
    // City class to store location and drawing information
    private class City {
        String name;
        int x, y;
        Color color;

        public City(String name, int x, int y, Color color) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    // Route class to represent connections between city & municipalities
    private class Route {
        City from;
        City to;
        int distance;

        public Route(City from, City to, int distance) {
            this.from = from;
            this.to = to;
            this.distance = distance;
        }
    }

    // Graph to represent city & municipalities connections
    private class CityGraph {
        private Map<String, City> cities;
        private List<Route> routes;

        public CityGraph() {
            cities = new HashMap<>();
            routes = new ArrayList<>();
        }

        public void addCity(String name, int x, int y, Color color) {
            cities.put(name, new City(name, x, y, color));
        }

        public void addRoute(String from, String to, int distance) {
            City fromCity = cities.get(from);
            City toCity = cities.get(to);
            routes.add(new Route(fromCity, toCity, distance));
        }

        public City getCity(String name) {
            return cities.get(name);
        }

        public List<Route> getRoutes() {
            return routes;
        }

        // Dijkstra's shortest path algorithm
        public Stack<City> findShortestPath(String start, String end) {
            Map<City, Integer> distances = new HashMap<>();
            Map<City, City> previousCity = new HashMap<>();
            PriorityQueue<City> pq = new PriorityQueue<>(
                Comparator.comparingInt(city -> distances.getOrDefault(city, Integer.MAX_VALUE))
            );

            City startCity = getCity(start);
            City endCity = getCity(end);

            // Initialize distances
            for (City city : cities.values()) {
                distances.put(city, city == startCity ? 0 : Integer.MAX_VALUE);
                pq.offer(city);
            }

            while (!pq.isEmpty()) {
                City current = pq.poll();
                
                if (current == endCity) break;

                if (distances.get(current) == Integer.MAX_VALUE) break;

                // Find connected routes
                for (Route route : routes) {
                    City neighbor = route.from == current ? route.to : 
                                    route.to == current ? route.from : null;
                    
                    if (neighbor != null) {
                        int alternateDistance = distances.get(current) + route.distance;
                        if (alternateDistance < distances.get(neighbor)) {
                            distances.put(neighbor, alternateDistance);
                            previousCity.put(neighbor, current);
                            pq.remove(neighbor);
                            pq.offer(neighbor);
                        }
                    }
                }
            }

            // Reconstruct path
            Stack<City> path = new Stack<>();
            City current = endCity;
            while (current != null) {
                path.push(current);
                current = previousCity.get(current);
            }

            return path;
        }
    }

    // Custom map drawing panel
    private class MapPanel extends JPanel {
        private List<City> highlightedCities;
        private List<Route> highlightedRoutes;

        public MapPanel() {
            setPreferredSize(new Dimension(600, 400));
            setBackground(Color.WHITE);
            highlightedCities = new ArrayList<>();
            highlightedRoutes = new ArrayList<>();
        }

        public void highlightRoute(List<City> cities, List<Route> routes) {
            highlightedCities = new ArrayList<>(cities);
            highlightedRoutes = new ArrayList<>(routes);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw all routes
            g2d.setColor(Color.LIGHT_GRAY);
            for (Route route : cityGraph.getRoutes()) {
                g2d.drawLine(route.from.x, route.from.y, route.to.x, route.to.y);
            }

            // Highlight selected routes
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.RED);
            for (Route route : highlightedRoutes) {
                g2d.drawLine(route.from.x, route.from.y, route.to.x, route.to.y);
            }

            // Draw all cities
            for (City city : cityGraph.cities.values()) {
                g2d.setColor(city.color);
                g2d.fillOval(city.x - 10, city.y - 10, 20, 20);
                
                // Draw city name
                g2d.setColor(Color.BLACK);
                g2d.drawString(city.name, city.x + 15, city.y);
            }

            // Highlight start and end cities
            for (City city : highlightedCities) {
                g2d.setColor(Color.GREEN);
                g2d.fillOval(city.x - 15, city.y - 15, 30, 30);
                
                // Draw city name in green
                g2d.setColor(Color.BLACK);
                g2d.drawString(city.name, city.x + 15, city.y);
            }
        }
    }

    // GUI Components
    private JTextField startCityField;
    private JTextField endCityField;
    private JTextArea routeDisplayArea;
    private MapPanel mapPanel;
    private CityGraph cityGraph;

    public CityAndMunicipalitiesRouteInBataan() {
        // Initialize the frame
        setTitle("City and Municipalities Route Planner in Bataan");
        setSize(600, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Initialize graph with sample cities and routes
        cityGraph = new CityGraph();
        initializeCityRoutes();

        // Create input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Start City / Municipality:"));
        startCityField = new JTextField();
        inputPanel.add(startCityField);

        inputPanel.add(new JLabel("End City / Municipality:"));
        endCityField = new JTextField();
        inputPanel.add(endCityField);

        JButton findRouteButton = new JButton("Find Route");
        findRouteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findRoute();
            }
        });
        inputPanel.add(findRouteButton);

        // Create route display area
        routeDisplayArea = new JTextArea();
        routeDisplayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(routeDisplayArea);

        // Create map panel
        mapPanel = new MapPanel();

        // Add components to frame
        add(inputPanel, BorderLayout.NORTH);
        add(mapPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
    }

    private void initializeCityRoutes() {
        // City & Municipalities with specific coordinates
        cityGraph.addCity("Dinalupihan", 330, 15, Color.BLUE);
        cityGraph.addCity("Orani", 460, 60, Color.BLUE);
        cityGraph.addCity("Samal", 465, 105, Color.BLUE);
        cityGraph.addCity("Abucay", 458, 160, Color.BLUE);
        cityGraph.addCity("Balanga", 460, 200, Color.BLUE);
        cityGraph.addCity("Pilar", 470, 225, Color.BLUE);
        cityGraph.addCity("Orion", 480, 265, Color.BLUE);
        cityGraph.addCity("Limay", 485, 320, Color.BLUE);
        cityGraph.addCity("Home", 490, 400, Color.BLUE);
        cityGraph.addCity("Mariveles", 350, 420, Color.BLUE);
        cityGraph.addCity("Bagac", 200, 280, Color.BLUE);
        cityGraph.addCity("Morong", 80, 185, Color.BLUE);
        cityGraph.addCity("Hermosa", 280, 40, Color.BLUE);

        // Add routes with distances
        cityGraph.addRoute("Dinalupihan", "Hermosa", 15);
        cityGraph.addRoute("Dinalupihan", "Orani", 17);
        cityGraph.addRoute("Dinalupihan", "Orion", 35);
        cityGraph.addRoute("Orani", "Samal", 26);
        cityGraph.addRoute("Samal", "Abucay", 5);
        cityGraph.addRoute("Abucay", "Balanga", 5);
        cityGraph.addRoute("Balanga", "Pilar", 2);
        cityGraph.addRoute("Pilar", "Orion", 9);
        cityGraph.addRoute("Orion", "Limay", 8);
        cityGraph.addRoute("Limay", "Home", 14);
        cityGraph.addRoute("Home", "Mariveles", 12);
        cityGraph.addRoute("Mariveles", "Bagac", 44);
        cityGraph.addRoute("Bagac", "Morong", 25);
        cityGraph.addRoute("Bagac", "Pilar", 26);
        cityGraph.addRoute("Morong", "Hermosa", 48);
    }

    private void findRoute() {
        String startCity = startCityField.getText().trim();
        String endCity = endCityField.getText().trim();

        if (startCity.isEmpty() || endCity.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both start and end cities.", 
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Find shortest path
            Stack<City> route = cityGraph.findShortestPath(startCity, endCity);
            
            if (route.size() <= 1) {
                routeDisplayArea.setText("No route found between " + startCity + " and " + endCity);
                mapPanel.highlightRoute(new ArrayList<>(), new ArrayList<>());
                return;
            }

            // Build route display and collect route cities and routes
            StringBuilder routeDisplay = new StringBuilder("Shortest Route: ");
            List<City> routeCities = new ArrayList<>();
            List<Route> routeConnections = new ArrayList<>();
            

            while (!route.isEmpty()) {
                City currentCity = route.pop();
                routeCities.add(currentCity);
                routeDisplay.append(currentCity.name);
                
                if (!route.isEmpty()) {
                    routeDisplay.append(" → ");
                    
                    // Find the route between current and next city
                    City nextCity = route.peek();
                    for (Route r : cityGraph.getRoutes()) {
                        if ((r.from == currentCity && r.to == nextCity) || 
                            (r.to == currentCity && r.from == nextCity)) {
                            routeConnections.add(r);
                            break;
                        }
                    }
                }
            }

            // Update route display and map
            routeDisplayArea.setText(routeDisplay.toString());
            mapPanel.highlightRoute(routeCities, routeConnections);

        } catch (Exception ex) {
            routeDisplayArea.setText("Error finding route: " + ex.getMessage());
            mapPanel.highlightRoute(new ArrayList<>(), new ArrayList<>());
        }
    }

    public static void main(String[] args) {
        // Ensure GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CityAndMunicipalitiesRouteInBataan().setVisible(true);
            }
        });
    }
}