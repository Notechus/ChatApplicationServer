# ChatApplicationServer
This is server for my ChatApplication(moved it from ChatApp project here)
 
 TODO:
   * add encrypting to packets(almost done), 
   * add user-user communication(necessary to add new packet type for this),
   * add acknowledgement packet,
   * get separate udp for ping and dc,
   * add logging system (i have scratch),
   * add exceptions management,
   * improve gui, add sounds,
   * add database management
   * change login for users from db, add registration, 
   * add new uid system,
   * unique id for user stored in db -> will provide friends and will help with login stuff. 
     Finally we might want to replace all chat with sth else like news feed.

Last changes:<br>
  Added some sort of encryption for packets so they cannot be sniffed.<br>
  Added javadoc comment.<br>
