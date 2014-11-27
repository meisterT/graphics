package se.kth.livetech.presentation.layout;

import java.awt.ScrollPane;

import javax.swing.JPanel;

import se.kth.livetech.properties.IProperty;
import se.kth.livetech.properties.PropertyListener;

import com.tightvnc.VncViewer;
import com.tightvnc.VncViewerFactory;

/**
 * @author auno
 */
@SuppressWarnings("serial")
public class VNCPresentation extends JPanel {
	private String host = "192.168.1.141";
	private String password = "";
	double zoom = 1;
	int portBase = 59000;
	int teamPort = 1;
		
	private VncViewer vv = null;
	private ScrollPane sp = null;
	PropertyListener hostChange, portChange, zoomChange, panXChange, teamChanger;
	
	
	public VNCPresentation(IProperty base) {
		IProperty vncProps = base.get("vnc");
		
		sp = new ScrollPane(ScrollPane.SCROLLBARS_NEVER);
		this.add(sp);
		
		//team change shortcut
		base.get("team.team").addPropertyListener(teamChanger = new PropertyListener() {
			@Override
			public void propertyChanged(IProperty changed) {
				if(changed.getValue().isEmpty())
					changed.setIntValue(96); //default
				teamPort = changed.getIntValue();
				connect();
			}
		});
		
		hostChange = new PropertyListener() {	
			@Override
			public void propertyChanged(IProperty changed) {
				if(changed.getValue().isEmpty())
					changed.setValue("192.168.1.141"); //default

				host = changed.getValue();
				connect();
			}
		};
		portChange = new PropertyListener() {	
			@Override
			public void propertyChanged(IProperty changed) {
				if(changed.getValue().isEmpty())
					changed.setIntValue(59000); //default
				portBase = changed.getIntValue();
				connect();
			}
		};
		zoomChange = new PropertyListener() {	
			@Override
			public void propertyChanged(IProperty changed) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (changed.getValue().isEmpty())
					changed.setDoubleValue(zoom = 1.0); //default
				else
					zoom = changed.getDoubleValue();
				connect();
			}
		};

		vncProps.get("host").addPropertyListener(hostChange);
		vncProps.get("port").addPropertyListener(portChange);
		vncProps.get("pz.zoom").addPropertyListener(zoomChange);
	}
	
	public void connect() {
		if (!this.isDisplayable())
			return;
		System.err.println("connect");
		sp.removeAll();
		
		if (vv != null) {
			vv.disconnect();
			vv.stop();
			vv.destroy();
			vv = null;
		}
	
		if (!host.equals("") && portBase + teamPort > 0) {
			vv = VncViewerFactory.createVncViewer(
					host, 
					portBase + teamPort, 
					(password == null) ? null : password,
					zoom,
					false);
			
			sp.add(vv);
			vv.init();
			vv.start();
			
			this.validate();
		}
	}
	
	@Override
	public void invalidate() {
//		Rectangle currentBounds = sp.getBounds();
		//save position
//		currentBounds.width = this.getBounds().width;
//		currentBounds.height = this.getBounds().height;
//		sp.setBounds(currentBounds);
		sp.setBounds(this.getBounds());
		sp.validate();
	}
}
