# DefectSystem
Information system for tracking defects in hotels

# Before the first start:
- Create MySQL database and create user with all permisions in it 
 (Default DB name: 'dis', user: 'disadmin'%'localhost', password: 'admin')
 CREATE DATABASE dis;
 CREATE USER 'disadmin'@'localhost' IDENTIFIED BY 'admin';
 grant all privileges on dis.* to 'disadmin'@'localhost';
 FLUSH PRIVILEGES;

- If the names are not default, change them in the src/main/resources/application.properties
- Register the TelegramBot at https://t.me/BotFather
- Write the name and token in the src/main/java/com/softserve/dis/bot/TelegramBot.java class (71 and 74 lines)

# To start an application need to run src/main/java/com/softserve/dis/DefectsSystemApplication.java class
# Go to http://localhost:8080 to view the site
