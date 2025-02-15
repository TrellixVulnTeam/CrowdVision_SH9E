package com.tcc.CrowdVision.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.tcc.CrowdVision.Repository.CameraRepository;
import com.tcc.CrowdVision.Repository.DetectionHistoryRepository;
import com.tcc.CrowdVision.Repository.DetectionRepository;
import com.tcc.CrowdVision.Server.Camera.Camera;
import com.tcc.CrowdVision.Server.Detection.Detection;
import com.tcc.CrowdVision.Server.Detection.DetectionHistory;
import com.tcc.CrowdVision.Server.Detection.DetectionManager;
import com.tcc.CrowdVision.Utils.DateUtils;

@RestController
@CrossOrigin
@RequestMapping("/detection")
public class DetectionController {
	
	@Autowired
	private DetectionRepository detectionRepository;
	
	@Autowired
	private DetectionHistoryRepository detectionHistoryRepository;
	
	@Autowired
	private CameraRepository cameraRepository;	
	
	@PostMapping("/add-frame/detected")
	public ResponseEntity<String> addicionar(@RequestBody Map<String, Object> detection) {
		try {
			double d = (double) detection.get("detectionScore");
			float detectionScore = (float)d;
			
			DetectionHistory detectionHistory = new DetectionHistory(
													(String) detection.get("frame"), 
													detectionScore, 
													DateUtils.convetStringToDate((String) detection.get("detectionTime"), "dd/MM/yyyy hh:mm:ss"), 
													DateUtils.convetStringToDate((String) detection.get("captureTime"), "dd/MM/yyyy hh:mm:ss"), 
													(String) detection.get("cameraId")
												);
			
			Optional<Camera> optionalCamera = cameraRepository.findById(detectionHistory.getCameraId());
						
			if (optionalCamera.isPresent()) {
				DetectionHistory detectionHistorySaved = detectionHistoryRepository.save(detectionHistory);
				
				Detection frameDetected = new Detection(detectionHistorySaved);
				
				detectionRepository.save(frameDetected);
				
				DetectionManager detectionManager = DetectionManager.getInstance();
				detectionManager.sendStatus();
				
				return ResponseEntity.ok("Detection saved");
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Camera Id");
		}
		catch (Exception e) {
			e.printStackTrace();	
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed trying to save on database");
		}

	}
	
	@GetMapping("/frames-from-camera")
	public ResponseEntity<String> framesFromCamera(@RequestParam String cameraId) {
		try {
			Optional<Camera> cameraOptional  = cameraRepository.findById(cameraId);
			if (cameraOptional.isPresent()) {
				Camera camera = cameraOptional.get();
				
				JSONObject json = new JSONObject();
				
				Gson gson = new Gson();
				String cameraString = gson.toJson(camera);
				
				json.put("camera", new JSONObject(cameraString));
				
				JSONArray detectionArray = new JSONArray();
				ArrayList<Detection> detections = detectionRepository.findDetectionByCameraId(cameraId);
				
				for(Detection detection: detections) {
					String detectionString = gson.toJson(detection);
					
					JSONObject detectionObject = new JSONObject(detectionString);
					detectionArray.put(detectionObject);
					
				}
				json.put("frames", detectionArray);
				return ResponseEntity.ok(json.toString());
			}
			
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid Camera Id");
		
		}
		catch (Exception e) {
			e.printStackTrace();	
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed trying to save on database");
		}
	}
	
	@GetMapping("/frames-from-user")
	public ResponseEntity<String> framesFromUser(@RequestParam(defaultValue = "none") String userId) {
		
		DetectionManager detectionManager = DetectionManager.getInstance();
		if (!userId.contains("none")) {
			return ResponseEntity.ok(detectionManager.getFrames(userId, false, null));
		}
		
		return ResponseEntity.badRequest().body("user id invalido");
	}
	
	@GetMapping("/history")
	public ResponseEntity<String> getHistory(@RequestParam(defaultValue = "none") String userId, @RequestParam(defaultValue = "none") String cameraId) {
		
		DetectionManager detectionManager = DetectionManager.getInstance();
		try {
			if (!userId.contains("none")) {
				
				if (!cameraId.contains("none"))
				{
					List<String> temp = new ArrayList<String>();
					temp.add(cameraId);
					return ResponseEntity.ok(detectionManager.getFrames(userId, true, temp));
				}
				return ResponseEntity.ok(detectionManager.getFrames(userId, true, null));
			}
			return ResponseEntity.badRequest().body("user id invalido");
		}
		catch(Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("user id invalido");
		}
		
		
	}
	
	@PostMapping("/update-status")
	public ResponseEntity<String> updateDetectionStatus(@RequestBody Map<String, Object> detectionUpdate) {
		try {
			if (detectionUpdate.containsKey("detectionId") && detectionUpdate.containsKey("detectionHistoryId") &&detectionUpdate.containsKey("detectionStatus")) {
				String detectionId = (String) detectionUpdate.get("detectionId");
				String detectionHistoryId = (String) detectionUpdate.get("detectionHistoryId");
				Boolean detectionStatus = (Boolean) detectionUpdate.get("detectionStatus");
				System.out.println("ID " + detectionId);
				System.out.println("STATUS " + detectionStatus);
				
				detectionRepository.deleteById(detectionId);
				
				if (detectionStatus.equals(false)) {
					Optional<DetectionHistory> optionalDetection = detectionHistoryRepository.findById(detectionHistoryId);

					if (optionalDetection.isPresent()) {
						DetectionHistory detectionHistory = optionalDetection.get();
						detectionHistory.setDetectionStatus(detectionStatus);
						detectionHistoryRepository.save(detectionHistory);
					}
				}
			
				DetectionManager detectionManager = DetectionManager.getInstance();
				detectionManager.sendStatus();
				
				return ResponseEntity.ok().body("Detection Updated");
			}		
			return ResponseEntity.badRequest().body("Invalid JSON");
		}
		catch(Exception e) {
			return ResponseEntity.badRequest().body("Invalid JSON");
		}
		
	}	
	
	@GetMapping("/test")
	public ResponseEntity<String> test() {
		DetectionManager detectionManager = DetectionManager.getInstance();
		detectionManager.sendStatus();
		return ResponseEntity.ok("ok");
	}
	
	/**
	 * Get detections statistics, if StartDate and EndDate is declared 
	 * filter this period.
	 * 
	 * @param cameraIds - An array of camera ids
	 * @param startDate - The initial date
	 * @param endDate - The final date
	 * 
	 * @return JSON
	 */
	@SuppressWarnings("unchecked")
	@PostMapping("/statistics")
	public ResponseEntity<String> getStatisticsPeriod(@RequestBody Map<String, Object> request) 
	{
		try
		{
			if(request.containsKey("cameraIds"))
			{
				ArrayList<String> cameraIds = (ArrayList<String>) request.get("cameraIds");
				
				if(!cameraIds.isEmpty()) {
					
					DetectionManager detectionManager = DetectionManager.getInstance();
					String responseJSON;
					
					if (request.containsKey("startDate") && request.containsKey("endDate")) {
						
						responseJSON = detectionManager.buildStatisticData(cameraIds, (String) request.get("startDate"), (String) request.get("endDate"));
					}
					else {
						responseJSON = detectionManager.buildStatisticData(cameraIds, null, null);
					}

					return ResponseEntity.ok(responseJSON);
				}
			}
		
			return ResponseEntity.badRequest().body("Invalid Request");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while tryning to get statistics");
		}
	}
}
