import common.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientDecoder extends SimpleChannelInboundHandler<FileUploadFile> {

    private NanoDropBoxClient nanoDBClient;
    public ClientDecoder(NanoDropBoxClient nanoDBClient) {
        this.nanoDBClient = nanoDBClient;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileUploadFile msg) throws Exception {
        System.out.println("Receive new message from Server.");
        if (msg.getComand().equals(nanoDBClient.getLogin())) {
            nanoDBClient.setValidate(msg.getComand());
        } else nanoDBClient.setValidate("FALSE");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("New channel is active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
