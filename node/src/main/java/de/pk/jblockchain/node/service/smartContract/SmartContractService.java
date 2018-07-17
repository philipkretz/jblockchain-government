package de.pk.jblockchain.node.service.smartContract;

import org.springframework.stereotype.Service;

import de.pk.jblockchain.node.service.smartContract.custom.CitizenContract;
import de.pk.jblockchain.node.service.smartContract.custom.CityContract;
import de.pk.jblockchain.node.service.smartContract.custom.DeclareAliveContract;
import de.pk.jblockchain.node.service.smartContract.custom.DeclareDeathContract;
import de.pk.jblockchain.node.service.smartContract.custom.DivorceContract;
import de.pk.jblockchain.node.service.smartContract.custom.HouseContract;
import de.pk.jblockchain.node.service.smartContract.custom.MarriageContract;
import de.pk.jblockchain.node.service.smartContract.custom.StreetContract;
import de.pk.jblockchain.node.service.smartContract.custom.UserMessageContract;

@Service
public class SmartContractService {

	private AbstractSmartContract[] contracts = { new CitizenContract(), new CityContract(), new DeclareAliveContract(),
			new DeclareDeathContract(), new DivorceContract(), new HouseContract(), new MarriageContract(),
			new StreetContract(), new UserMessageContract(), };

	public Boolean validateContract(String message) {
		Boolean isValid = false;
		for (AbstractSmartContract contract : contracts) {
			if (message.startsWith(contract.getAbbreviation())) {
				return contract.checkContract(message);
			}
		}
		return isValid;
	}

}
