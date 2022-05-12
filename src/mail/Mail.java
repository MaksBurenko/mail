package mail;
import  java.util.logging.*;

import static java.lang.String.format;

public class Mail {

    /*Интерфейс, который задает класс,
    который может каким-либо образом обработать почтовый объект.*/
    public static interface MailService {
        Sendable processMail(Sendable mail);
    }

    /*Класс, в котором скрыта логика настоящей почты*/
    public static class RealMailService implements MailService {
        @Override
        public Sendable processMail(Sendable mail) {
            // Здесь описан код настоящей системы отправки почты.
            return mail;
        }
    }

    /*Интерфейс: сущность, которую можно отправить по почте.
У такой сущности можно получить от кого и кому направляется письмо.*/
    public static interface Sendable {
        String getFrom();
        String getTo();
    }

    /*Абстрактный класс,который позволяет абстрагировать логику хранения
источника и получателя письма в соответствующих полях класса.*/
    public static abstract class AbstractSendable implements Sendable {

        protected final String from;
        protected final String to;

        public AbstractSendable(String from, String to) {
            this.from = from;
            this.to = to;
        }
        @Override
        public String getFrom() {
            return from;
        }
        @Override
        public String getTo() {
            return to;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AbstractSendable that = (AbstractSendable) o;

            if (!from.equals(that.from)) return false;
            if (!to.equals(that.to)) return false;

            return true;
        }
    }

    /*Письмо, у которого есть текст,
    который можно получить с помощью метода `getMessage`*/
    public static class MailMessage extends AbstractSendable {

        private final String message;

        public MailMessage(String from, String to, String message) {
            super(from, to);
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            MailMessage that = (MailMessage) o;

            if (message != null ? !message.equals(that.message) : that.message != null) return false;

            return true;
        }
    }

    /*Посылка, содержимое которой можно получить с помощью метода `getContent`*/
    public static class MailPackage extends AbstractSendable {
        private final Package content;

        public MailPackage(String from, String to, Package content) {
            super(from, to);
            this.content = content;
        }
        public Package getContent() {
            return content;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            MailPackage that = (MailPackage) o;

            if (!content.equals(that.content)) return false;

            return true;
        }
    }

    /*Класс, который задает посылку.
    У посылки есть текстовое описание содержимого и целочисленная ценность.*/
    public static class Package {
        private final String content;
        private final int price;

        public Package(String content, int price) {
            this.content = content;
            this.price = price;
        }
        public String getContent() {
            return content;
        }
        public int getPrice() {
            return price;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Package aPackage = (Package) o;

            if (price != aPackage.price) return false;
            if (!content.equals(aPackage.content)) return false;

            return true;
        }
    }

    public static class UntrustworthyMailWorker implements MailService {

        MailService [] ms;
       public UntrustworthyMailWorker(MailService [] ms) {
           this.ms = ms;
        }

        private MailService real = new RealMailService();

        //todo  последовательно передает этот объект набору третьих лиц, а затем, в конце концов,
        // передает получившийся объект непосредственно экземпляру RealMailService

        @Override
        public Sendable processMail(Sendable mail) {
            Sendable m = mail;
            for (MailService temp : ms) {
                m = temp.processMail(m);
            }
            return real.processMail(m);
        }
        //todo метод getRealMailService, который возвращает ссылку на внутренний экземпляр RealMailService.
        public MailService getReal() {
            return real;
        }
    }

    public static class Spy implements MailService{

        private Logger logger;

        public Spy(Logger logger) {
            this.logger = logger;
        }

        @Override
        public Sendable processMail(Sendable mail) {

            if(mail instanceof MailMessage) {
                if (mail.getFrom().equals("Austin Powers") || mail.getTo().equals("Austin Powers")) {
                    logger.warning("Detected target mail correspondence: from " + mail.getFrom() + " " + "to " + mail.getTo() + " " + ((MailMessage) mail).getMessage());
                } else {
                    logger.info("Usual correspondence: from " + mail.getFrom() + " " + "to " + mail.getTo());
                }
            }
            return mail;
        }
    }

    public static class Thief implements MailService{

        int minPrice;
        int stolenValue = 0;

        public Thief (int minPrice) {
            this.minPrice = minPrice;
        }

        public int getStolenValue() {
            return this.stolenValue;
        }


        @Override
        public Sendable processMail(Sendable mail) {
            if(mail instanceof Package) {
                if(minPrice >= ((Package) mail).getPrice()){
                    this.stolenValue = this.stolenValue + ((Package) mail).getPrice();
                    Package p = new Package("stones instead of " + ((Package) mail).content, 0);
                    MailPackage m = new MailPackage (mail.getFrom(), mail.getTo(), p);
                    return  m;
                }
            }
            return mail;
        }
    }

    public static class Inspector implements MailService   {

        @Override
        public Sendable processMail(Sendable mail) {
            if (mail instanceof MailPackage) {
                String c = ((MailPackage) mail).getContent().getContent();
                if (c.contains("weapons") || (c.contains("banned substance")){
                    throw new IllegalPackageException();
                }
                if (c.contains("stones")) {
                    throw new StolenPackageException();
                }
                return mail;
            }
        }
    }
    public static class IllegalPackageException extends RuntimeException {
        public IllegalPackageException () {
        }
    }
    public static class StolenPackageException extends RuntimeException {
        public StolenPackageException () {
        }
    }
}