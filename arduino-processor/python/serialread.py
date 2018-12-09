import serial
import io

port = serial.Serial("/dev/ttyUSB0", 9600, timeout=5)
while True:
    # Catch just in case we exit irregularly
    port.close()
    data = open("received.txt", "w")
    port.open()
    string = port.read_until("Z")
    new_data = string.decode("ascii") 
    new_data_table = new_data.split("\r\n")
    new_data_table.remove("X")
    new_data_table.remove("Z")
    for i in new_data_table:
        data.write(i+"\n")
    port.close()
    data.close()
