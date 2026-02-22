DB_NAME='mydb'

install:
	@echo "[!] Start compiling ..."
	@mvn clean install
	@echo "[+] End compiling"

run:
	@GDK_SCALE=2 mvn exec:java -Dexec.mainClass="view.Form_Main"

connect-db:
	@mysql -u root  ${B_NAME}

all: run