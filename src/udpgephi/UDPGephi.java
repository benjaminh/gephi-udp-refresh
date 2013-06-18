/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package udpgephi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.swing.JFrame;
import org.gephi.filters.api.*;
import org.gephi.filters.plugin.graph.EgoBuilder.EgoFilter;
import org.gephi.graph.api.*;
import org.gephi.io.importer.api.*;
import org.gephi.io.processor.plugin.*;
import org.gephi.preview.api.*;
import org.gephi.preview.types.*;
import org.gephi.project.api.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import processing.core.*;

/**
 *
 * @author bhervy
 */
public class UDPGephi {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception
      {
         //Init a project - and therefore a workspace
         ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
         pc.newProject();
         Workspace workspace = pc.getCurrentWorkspace();
 
         //Import file
         ImportController importController = Lookup.getDefault().lookup(ImportController.class);
         Container container;
         try {
            File file = new File("/home/bhervy/dev/UDPGephi/src/udpgephi/saint-similien.gexf");
            container = importController.importFile(file);
         } catch (Exception exc) {
            Exceptions.printStackTrace(exc);
            return;
         }         
                           
         //Append imported data to GraphAPI
                  importController.process(container, new DefaultProcessor(), workspace);
         //Preview configuration
                  PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
         //Ego filter
                  FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
                  GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
         //Preview configuration
                  PreviewModel previewModel = previewController.getModel();
                  previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
                  previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.WHITE));
                  previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
                  previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
                  previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 10f);
                  previewModel.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);
                  previewController.refreshPreview(); 
         //New Processing target, get the PApplet
                  ProcessingTarget target = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
                  PApplet applet = target.getApplet();
                  applet.init();
                  try {
                    Thread.sleep(100);
                  } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                  }
                  previewController.render(target);
                  target.refresh();
                  target.resetZoom();
         //Define JFrame
                  JFrame frame = new JFrame("Test Preview");
                  frame.setLayout(new BorderLayout());
                  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                  
                  frame.add(applet, BorderLayout.CENTER);
                  frame.pack();
                  frame.setVisible(true);
         
         //UDP receive
         DatagramSocket receiverSocket = new DatagramSocket(6005);
         byte[] receiveData = new byte[8];
         while(true)
         {
                  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                  receiverSocket.receive(receivePacket);
                  String message = new String( receivePacket.getData(), 0, receivePacket.getLength());
                  System.out.println("RECEIVED: " + message);
                  InetAddress IPAddress = receivePacket.getAddress();
                  int port = receivePacket.getPort();
                  String capitalizedSentence = message.toUpperCase();
                  Integer nodeID = Integer.parseInt(message.trim().replace("''","")); //Seems message contains guillemets at the beginning of the string
 
                  //Ego Filter
                  //System.out.println("RECEIVED: " + nodeID);
                  EgoFilter egoFilter = new EgoFilter();
                  egoFilter.setPattern(nodeID.toString());//Regex accepted
                  egoFilter.setDepth(1);
                  Query queryEgo = filterController.createQuery(egoFilter);
                  GraphView viewEgo = filterController.filter(queryEgo);
                  graphModel.setVisibleView(viewEgo);//Set the filter result as the visible view
                  
               }
      }
}
