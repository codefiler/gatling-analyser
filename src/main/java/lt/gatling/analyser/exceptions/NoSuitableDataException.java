package lt.gatling.analyser.exceptions;

public class NoSuitableDataException extends Exception{
    public NoSuitableDataException() {
        super("В логе не нашлось записей, удовлетворяющих заданному диапазону");
    }
}
