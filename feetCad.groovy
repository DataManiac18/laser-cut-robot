import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import eu.mihosoft.vrl.v3d.parametrics.*;
import javafx.scene.paint.Color;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;


class Feet implements ICadGenerator, IParameterChanged{
	//First we load teh default cad generator script 
	ICadGenerator defaultCadGen=(ICadGenerator) ScriptingEngine
	                    .gitScriptRun(
                                "https://github.com/DataManiac18/laser-cut-robot.git", // git location of the library
	                              "laserCutCad.groovy" , // file to load
	                              null
                        )
	LengthParameter thickness 		= new LengthParameter("Material Thickness",3.15,[10,1])
	LengthParameter headDiameter 		= new LengthParameter("Head Dimeter",100,[200,50])
	LengthParameter snoutLen 		= new LengthParameter("Snout Length",63,[200,50])
	LengthParameter jawHeight 		= new LengthParameter("Jaw Height",32,[200,10])
	LengthParameter leyeDiam 		= new LengthParameter("Left Eye Diameter",35,[headDiameter.getMM()/2,29])
	LengthParameter reyeDiam 		= new LengthParameter("Right Eye Diameter",35,[headDiameter.getMM()/2,29])
	LengthParameter eyeCenter 		= new LengthParameter("Eye Center Distance",headDiameter.getMM()/2,[headDiameter.getMM(),headDiameter.getMM()/2])
	StringParameter servoSizeParam 			= new StringParameter("hobbyServo Default","towerProMG91",Vitamins.listVitaminSizes("hobbyServo"))
	StringParameter boltSizeParam 			= new StringParameter("Bolt Size","M3",Vitamins.listVitaminSizes("capScrew"))

	HashMap<String, Object>  boltMeasurments = Vitamins.getConfiguration( "capScrew",boltSizeParam.getStrValue())
	HashMap<String, Object>  nutMeasurments = Vitamins.getConfiguration( "nut",boltSizeParam.getStrValue())
	//println boltMeasurments.toString() +" and "+nutMeasurments.toString()
	double boltDimeMeasurment = boltMeasurments.get("outerDiameter")
	double nutDimeMeasurment = nutMeasurments.get("width")
	double nutThickMeasurment = nutMeasurments.get("height")
	private TransformNR offset =BowlerStudio3dEngine.getOffsetforvisualization().inverse();
	ArrayList<CSG> headParts =null
	@Override 
	public ArrayList<CSG> generateCad(DHParameterKinematics d, int linkIndex) {
		ArrayList<CSG> allCad=defaultCadGen.generateCad(d,linkIndex);
		ArrayList<DHLink> dhLinks=d.getChain().getLinks();
		LinkConfiguration conf = d.getLinkConfiguration(linkIndex);
		DHLink dh = dhLinks.get(linkIndex)
		HashMap<String, Object> shaftmap = Vitamins.getConfiguration(conf.getShaftType(),conf.getShaftSize())
		double hornOffset = shaftmap.get("hornThickness")	
		
		// creating the servo
		CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		.transformed(new Transform().rotZ(90))
		
		double servoTop = servoReference.getMaxZ()
		CSG horn = Vitamins.get(conf.getShaftType(),conf.getShaftSize()).hull()
		//the if statements below create the different leg links depending on their position
		if(linkIndex ==dhLinks.size()-1){
			println "Found foot limb" 
			CSG foot =new Cylinder(15,15,thickness.getMM(),(int)hornOffset).toCSG()
															   .movez(-hornOffset/2)
			CSG shoulder = new Cube(50,dh.getR(),hornOffset).toCSG()
											.movey(dh.getR()/2)
			
			shoulder = defaultCadGen.moveDHValues(shoulder,dh)
			shoulder = shoulder.difference(horn.movex(-dh.getR()))
			defaultCadGen.add(allCad,foot,dh.getListener())
			defaultCadGen.add(allCad,shoulder,dh.getListener())
			
		}
		else if(linkIndex == 0){
			CSG connector = new Cube(dh.getR(),50,hornOffset).toCSG()
											.movey(dh.getR()/2)
			connector = defaultCadGen.moveDHValues(connector,dh)
			connector = connector.difference(horn.movex(-dh.getR()))
			defaultCadGen.add(allCad,connector,dh.getListener())
		}
		else
		{
			CSG connector = new Cube(50,dh.getR(),hornOffset).toCSG()
											.movey(dh.getR()/2)
			connector = defaultCadGen.moveDHValues(connector,dh)
			connector = connector.difference(horn.movex(-dh.getR()))
			connector = connector.difference(horn)
			defaultCadGen.add(allCad,connector,dh.getListener())
		}
	
		return allCad;
	}
	@Override 
	public ArrayList<CSG> generateBody(MobileBase b ) {
		ArrayList<CSG> allCad=defaultCadGen.generateBody(b);
		
		return allCad;
	}
	/**
	 * This is a listener for a parameter changing
	 * @param name
	 * @param p
	 */
	 
	public void parameterChanged(String name, Parameter p){
		//new RuntimeException().printStackTrace(System.out);
		println "headParts was set to null from "+name
		new Exception().printStackTrace(System.out)
		headParts=null
	}
};

return new Feet()//Your code here
