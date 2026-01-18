// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract Payment {
    address public owner;
    mapping(address => uint256) public balances;

    event Paid(address indexed to, uint255 amount);

    constructor() {
        owner = msg.sender;
    }

    function pay(address to, uint256 amount) external {
        require(amount > 0, "Amount must be positive");
        balances[to] += amount;
        emit Paid(to, amount);
    }

    function getBalance(address user) external view returns (uint256) {
        return balances[user];
    }
}
