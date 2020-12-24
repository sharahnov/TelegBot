import java.sql.*;
import java.util.HashMap;

public class DBworker {

    private final String URL = "jdbc:mysql://localhost:3306/mydb";
    private final String USERNAME = "root";
    private final String PASSWORD = "root";
    private final String INSERT = "INSERT INTO botdb (id,PvPwin, PvPdraw, PvPlose, PvEwin, PvEdraw, PvElose, state) VALUES(?,?,?,?,?,?,?,?)";
    private final String UPDATE = "UPDATE botdb set PvPwin = ?, PvPdraw = ?, PvPlose = ?, PvEwin = ?, PvEdraw = ?, PvElose = ?, messageId = ?, state = ? where id = ?";

    private Connection connection;

    DBworker(){
        try {
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            if (this.connection.isClosed()){
                System.err.println("Connection");
            }
        } catch (SQLException e){
            e.printStackTrace();
            System.err.println("No driver");
        }
    }

    public void closeConnection(){
        try {
            this.connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void insertUser(User user){
        try {
            PreparedStatement statement = connection.prepareStatement(INSERT);
            statement.setInt(1, Math.toIntExact(user.getId()));
            for (int i = 2; i < 8; i++){
                statement.setInt(i, 0);
            }
            statement.setString(8, "default");
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void updateUser(User user){
        try {
            PreparedStatement statement = connection.prepareStatement(UPDATE);
            statement.setInt(9, Math.toIntExact(user.getId()));
            int index = 1;
            for (int i = 0; i < 2; i++){
                for (int j = 0; j <3; j++) {
                    statement.setInt(index, user.getStatistics()[i][j]);
                    System.out.println(user.getStatistics()[i][j]);
                    index++;
                }
            }
            statement.setString(8, user.state);
            statement.setInt(7, user.messageId);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public HashMap<Long, User> getUsers(){
        HashMap<Long, User> users = new HashMap<>();
        String query = "select * from botdb";
        try {
            Statement statement = this.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()){
                long id = resultSet.getInt(1);
                int[][] statistics = new int[2][3];
                for (int i = 0; i < 6; i++){
                    int x = i / 3;
                    int y = i % 3;
                    statistics[x][y] = resultSet.getInt(i + 2);
                }
                String state = resultSet.getString("state");
                int messageId = resultSet.getInt("messageId");
                User user = new User(id, statistics);
                user.state = state;
                user.messageId = messageId;
                users.put(id, user);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return users;
    }
}
