/*
 * Tentackle - https://tentackle.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.tentackle.bundlemonkey.gui.main;

import org.tentackle.common.TentackleRuntimeException;
import org.tentackle.fx.BundleMonkeyHelper;
import org.tentackle.io.SocketChannelDispatcher;
import org.tentackle.log.Logger;

import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Provides a unix-domain socket server that receives navigation events from a Tentackle FX client application.
 */
public class NavigationServer {

  private static final Logger LOGGER = Logger.get(NavigationServer.class);
  private static final int BUF_SIZE = 1024;

  private final Path socketPath;
  private final SocketChannelDispatcher dispatcher;


  public NavigationServer(Consumer<String> uiListener) {
    socketPath = BundleMonkeyHelper.createSocketPath();
    removeSocket();

    dispatcher = new SocketChannelDispatcher("Navigation Server", UnixDomainSocketAddress.of(socketPath), () -> channel -> {
      try {
        String hierarchyDump = readHierarchyDump(channel);
        uiListener.accept(hierarchyDump);
        channel.close();
      }
      catch (IOException iox) {
        LOGGER.warning("closing client channel failed", iox);
      }
    });
  }

  public void shutdown() {
    dispatcher.shutdown();
    removeSocket();
  }

  private void removeSocket() {
    try {
      Files.deleteIfExists(socketPath);
    }
    catch (IOException iox) {
      throw new TentackleRuntimeException("cannot remove " + socketPath);
    }
  }

  private String readHierarchyDump(SocketChannel channel) throws IOException {
    StringBuilder buf = new StringBuilder();
    ByteBuffer buffer = ByteBuffer.allocate(BUF_SIZE);
    int bytesRead;
    while ((bytesRead = channel.read(buffer.clear())) > 0) {
      byte[] bytes = new byte[bytesRead];
      buffer.flip();
      buffer.get(bytes);
      buf.append(new String(bytes));
    }
    return buf.toString();
  }

}
