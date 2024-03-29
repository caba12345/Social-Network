package com.example.laboratorjavafx.repository.database;

import com.example.laboratorjavafx.Paging.Page;
import com.example.laboratorjavafx.Paging.Pageable;
import com.example.laboratorjavafx.Paging.PagingRepository;
import com.example.laboratorjavafx.domain.User;
import com.example.laboratorjavafx.repository.Repository;

import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class UserDbRepository implements PagingRepository<UUID ,User> {
    private String url;
    private String user;
    private String password;


    public UserDbRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());

            StringBuilder hexHash = new StringBuilder();
            for (byte b : hashBytes) {
                hexHash.append(String.format("%02x", b));
            }

            return hexHash.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<User> findOne(UUID id) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement statement  = connection.prepareStatement("SELECT * FROM Users WHERE UUID=?::uuid");)
        {
            //statement.setLong(1,id);
            statement.setObject(1,id.toString());
            ResultSet r = statement.executeQuery();
            if (r.next()){
                String FirstName = r.getString("FirstName");
                String LastName = r.getString("LastName");
                String email = r.getString("email");
                String passwordUser = r.getString("password");
                User u1 = new User(id, FirstName, LastName, email, passwordUser);
                return Optional.of(u1);
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        List<User> listaUseri = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement pagePreparedStatement  = connection.prepareStatement("SELECT * FROM Users " + "LIMIT ? OFFSET ?");
            PreparedStatement countPreparedStatement = connection.prepareStatement("SELECT COUNT(*) AS count FROM Users");
        ){
            pagePreparedStatement.setInt(1, pageable.getPageSize());
            pagePreparedStatement.setInt(2, pageable.getPageSize() * pageable.getPageNumber());
            try(ResultSet pageResultSet = pagePreparedStatement.executeQuery();
                ResultSet countResultSet = countPreparedStatement.executeQuery();) {
                while (pageResultSet.next()) {
                    UUID id = (UUID) pageResultSet.getObject("UUID");
                    String FirstName = pageResultSet.getString("FirstName");
                    String LastName = pageResultSet.getString("LastName");
                    String email = pageResultSet.getString("email");
                    String passwordUser = pageResultSet.getString("password");
                    User u1 = new User(id, FirstName, LastName, email, passwordUser);
                    listaUseri.add(u1);
                }
                int totalCount = 0;
                if (countResultSet.next()) {
                    totalCount = countResultSet.getInt("count");
                }

                return new Page<>(listaUseri, totalCount);
            }
        }
        catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<User> findAll() {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement statement  = connection.prepareStatement("SELECT * FROM Users");)
        {
            ArrayList<User> list = new ArrayList<>();
            ResultSet r = statement.executeQuery();
            while (r.next()){
                UUID id = (UUID) r.getObject("UUID");
                String FirstName = r.getString("FirstName");
                String LastName = r.getString("LastName");
                String email = r.getString("email");
                String passwordUser = r.getString("password");
                User u1 = new User(id, FirstName, LastName, email, passwordUser);
                list.add(u1);
            }
            return list;
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> save(User entity) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement statement  = connection.prepareStatement("INSERT INTO Users(UUID,FirstName,LastName,Email,Password) VALUES (?,?,?,?,?)");)
        {
            statement.setObject(1,entity.getId());
            statement.setString(2,entity.getFirstName());
            statement.setString(3,entity.getLastName());
            statement.setString(4,entity.getEmail());
            statement.setString(5,hashPassword(entity.getPassword()));


            //statement.setInt(3,entity.getYear());
            int affectedRows = statement.executeUpdate();
            return affectedRows!=0? Optional.empty():Optional.of(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }    }

    @Override
    public Optional<User> delete(UUID uuid) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement statement  = connection.prepareStatement("DELETE FROM Users WHERE UUID = ?::uuid");)
        {
            var cv = findOne(uuid);
            //statement.setLong(1,uuid);
            statement.setObject(1,uuid.toString());
            int affectedRows = statement.executeUpdate();
            return affectedRows==0? Optional.empty():cv;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> update(User entity) {
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement statement  = connection.prepareStatement("UPDATE Users SET FirstName = ?, LastName = ? WHERE UUID = ?");)
        {
            statement.setString(1,entity.getFirstName());
            statement.setString(2,entity.getLastName());
            statement.setObject(3,entity.getId());

            int affectedRows = statement.executeUpdate();
            return affectedRows!=0? Optional.empty():Optional.of(entity);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size(){
        try(Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement statement  = connection.prepareStatement("SELECT COUNT(*) FROM Users");)
        {
            ResultSet r = statement.executeQuery();
            if (r.next()){
                return r.getInt(1);
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
        return 0;
    }
}